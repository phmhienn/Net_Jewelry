"""Generate API reference and Postman requests from target/openapi.json exported by tests."""
from pathlib import Path
import copy
import json
import re

backend = Path(__file__).resolve().parents[1]
api = json.loads((backend / 'target/openapi.json').read_text(encoding='utf-8'))
schemas = api['components']['schemas']

def sample(schema, key=''):
    if '$ref' in schema:
        return sample(schemas[schema['$ref'].split('/')[-1]], key)
    if 'anyOf' in schema:
        return sample(next((s for s in schema['anyOf'] if s.get('type') != 'null'), {}), key)
    if schema.get('properties') is not None:
        return {name: sample(value, name) for name, value in schema['properties'].items()
                if name in schema.get('required', [])}
    values = {'payment':'COD', 'email':'{{customer_email}}', 'username':'{{customer_username}}',
              'password':'{{customer_password}}', 'currentPassword':'{{customer_password}}',
              'newPassword':'{{new_password}}', 'token':'{{reset_token}}', 'phone':'0901234567',
              'city':'Hồ Chí Minh','district':'Quận 1','ward':'Bến Nghé','street':'Địa chỉ của bạn',
              'sku':'SP-{{$randomInt}}','name':'Nhập tên','content':'Nhập nội dung','title':'Tiêu đề',
              'slug':'gioi-thieu','url':'{{image_url}}','image':'{{image_url}}',
              'code':'{{coupon_code}}','carrier':'Đơn vị vận chuyển','trackingCode':'{{tracking_code}}',
              'price':1500000,'value':10,'stars':5,'quantity':1,'threshold':5,'minimumOrder':0,
              'material':'Vàng 18K','buyPrice':10000000,'sellPrice':10200000,'unit':'VND/chỉ',
              'startsAt':'{{starts_at}}','endsAt':'{{ends_at}}'}
    if key in values: return values[key]
    if schema.get('enum'): return schema['enum'][0]
    typ = schema.get('type')
    if isinstance(typ,list): typ=next((t for t in typ if t!='null'),None)
    if typ == 'array': return []
    if typ == 'boolean': return True
    if typ in ('integer','number'): return 1
    if schema.get('format') == 'date': return '{{date}}'
    if schema.get('format') == 'date-time': return '{{starts_at}}'
    return 'Nhập giá trị'

def variable(path, name):
    if name=='slug': return 'content_slug'
    if name in ('variantId','productId','customerId'): return re.sub(r'([A-Z])',r'_\1',name).lower()
    if '/wishlist/' in path: return 'product_id'
    resources = {'orders':'order_id','cart/items':'cart_item_id','products':'product_id',
        'variants':'variant_id','product-images':'product_image_id','review-images':'review_image_id',
        'payments':'payment_id','reviews':'review_id','addresses':'address_id','categories':'category_id',
        'brands':'brand_id','gold-prices':'gold_price_id','staff':'staff_id','customers':'customer_id',
        'coupons':'coupon_id','banners':'banner_id','contents':'content_id'}
    return next((value for resource,value in resources.items() if '/'+resource+'/' in path),name)

groups={}; rows=[]; variables=set()
for path,item in api['paths'].items():
    for method,operation in item.items():
        if method not in ('get','post','put','patch','delete'):continue
        roles=operation.get('x-roles',[])
        token='customer' if 'KHACH_HANG' in roles else 'staff' if 'NHAN_VIEN' in roles else 'manager'
        parts=path.split('/'); module=parts[3] if parts[2]=='admin' else parts[2]
        def replace_id(match):
            name=variable(path,match[1]);variables.add(name);return '{{'+name+'}}'
        endpoint=re.sub(r'\{([^}]+)\}',replace_id,path)
        query=[{'key':p['name'],'value':str(p.get('schema',{}).get('default','')),'disabled':'default' not in p.get('schema',{})}
               for p in operation.get('parameters',[]) if p['in']=='query']
        request={'method':method.upper(),'header':[],
          'url':{'raw':'{{base_url}}'+endpoint,'host':['{{base_url}}'],'path':endpoint.lstrip('/').split('/'),'query':query},
          'description':operation.get('description',''),
          'auth':{'type':'bearer','bearer':[{'key':'token','value':'{{'+token+'_token}}','type':'string'}]} if roles else {'type':'noauth'}}
        content=operation.get('requestBody',{}).get('content',{})
        if 'application/json' in content:
            body=sample(content['application/json']['schema'])
            if path=='/api/auth/login': body={'identifier':'{{customer_identifier}}','password':'{{customer_password}}'}
            if path=='/api/auth/staff/login': body={'username':'{{staff_username}}','password':'{{staff_password}}'}
            request['body']={'mode':'raw','raw':json.dumps(body,ensure_ascii=False,indent=2),'options':{'raw':{'language':'json'}}}
            request['header'].append({'key':'Content-Type','value':'application/json'})
        if 'multipart/form-data' in content:request['body']={'mode':'formdata','formdata':[{'key':'file','type':'file','src':[]}]}
        events=[]
        if path.endswith('/login'):
            events.append({'listen':'test','script':{'type':'text/javascript','exec':['if(pm.response.code===200){const d=pm.response.json().data;pm.environment.set(d.user.role==="KHACH_HANG"?"customer_token":d.user.role==="QUAN_LY"?"manager_token":"staff_token",d.accessToken);}']}})
        if path=='/api/orders' and method=='post':
            request['header'].append({'key':'Idempotency-Key','value':'{{idempotency_key}}'})
            events.append({'listen':'prerequest','script':{'type':'text/javascript','exec':['if(!pm.environment.get("idempotency_key"))pm.environment.set("idempotency_key",pm.variables.replaceIn("{{$guid}}"));']}})
            events.append({'listen':'test','script':{'type':'text/javascript','exec':['if(pm.response.code===201){const d=pm.response.json().data;pm.environment.set("order_id",d.id);pm.environment.set("payment_id",d.payment.id);}']}})
        entry={'name':method.upper()+' '+path,'request':request}
        if events:entry['event']=events
        groups.setdefault(module.upper(),[]).append(entry)
        rows.append('| '+method.upper()+' | `'+path+'` | '+(', '.join(roles) or 'Public')+' |')

for role in ('staff', 'manager'):
    entry=copy.deepcopy(next(e for e in groups['AUTH'] if e['name']=='POST /api/auth/login'))
    entry['name']+=' — '+('NHAN_VIEN' if role=='staff' else 'QUAN_LY')
    entry['request']['body']['raw']=json.dumps({'identifier':'{{'+role+'_identifier}}','password':'{{'+role+'_password}}'},indent=2)
    groups['AUTH'].append(entry)
collection={'info':{'name':'Jewelry Store — 25 bảng','schema':'https://schema.getpostman.com/json/collection/v2.1.0/collection.json',
 'description':'Điền tài khoản và ID từ database của bạn. Đặt mới chỉ COD. Đổi Idempotency-Key cho đơn mới; giữ nguyên khi retry. Không có API simulate.'},
 'item':[{'name':name,'item':items} for name,items in groups.items()]}
values={'base_url':'http://127.0.0.1:8080',**{key:'' for key in variables}}
for key in ('customer_identifier','staff_identifier','manager_identifier','customer_email','customer_username','customer_password','staff_username','staff_password','manager_username','manager_password','customer_token','staff_token','manager_token','new_password','reset_token','image_url','coupon_code','tracking_code','idempotency_key','starts_at','ends_at','date'):values[key]=''
env={'name':'Jewelry local','values':[{'key':key,'value':value,'type':'secret' if 'password' in key or 'token' in key else 'default','enabled':True} for key,value in values.items()],'_postman_variable_scope':'environment'}
for name,data in [('openapi.json',api),('postman/JewelryStore.postman_collection.json',collection),('postman/local.postman_environment.json',env)]:
    (backend/name).write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
intro='''# REST API — schema 25 bảng

Base URL: http://127.0.0.1:8080. Success: `{success:true,message,data}`. Lỗi: `{timestamp,status,message,path,fieldErrors?}`. Không trả entity/password. Phân trang bắt đầu 0, size 1–100.

JWT: Authorization: Bearer token. Vai trò DB: KHACH_HANG, NHAN_VIEN, QUAN_LY. Quản lý có quyền của nhân viên; dữ liệu cá nhân luôn kiểm tra chủ sở hữu. CUSTOMER của bản cũ đã được thay bằng KHACH_HANG.

## Hợp đồng cần chú ý

- Login: POST /api/auth/login {identifier,password}; identifier nhận email hoặc tên đăng nhập cho cả ba vai trò. API vẫn nhận email/username làm alias để tương thích. /api/auth/staff/login đã deprecated. GET/PUT /api/account/profile và avatar dành cho cả ba vai trò, chỉ tài khoản đang đăng nhập.
- Register: name/email/password/username; username tùy chọn đối với client cũ, server sinh mã duy nhất nếu bỏ trống. Frontend mới yêu cầu nhập tên đăng nhập. Không nhận role từ khách.
- Products: keyword/category/brand/material/minPrice/maxPrice/sort/page/size. Giá lọc/sắp xếp là giá thấp nhất của biến thể đang bán, dự phòng giá sản phẩm. salePrice là giá khuyến mãi, null nghĩa không có; biến thể có status DANG_BAN/NGUNG_BAN.
- Cart: thêm {variantId,quantity}; sửa {quantity}; PATCH selection nhận {selected:true/false}. Giá, tổng và tồn do server đọc DB.
- Coupon quote: POST /api/coupons/quote {code}. Tổng dựa trên các dòng được chọn trong giỏ server; client không gửi tổng tiền. Quote không giữ lượt mã.
- Order: {address:{name,phone,city,district,ward,street},payment:"COD",note?,couponCode?}, header Idempotency-Key 8–100 ký tự chữ/số/gạch. Retry cùng key/body trả cùng đơn. Chỉ xóa dòng đã mua khỏi giỏ. Đơn trả code, discount, couponCode, payment, delivery và snapshot dòng hàng.
- Trạng thái đơn: CHO_XAC_NHAN → DA_XAC_NHAN → DANG_XU_LY → DANG_GIAO_HANG → HOAN_THANH; có thể DA_HUY trước giao và trước thu tiền. Không đổi trạng thái tùy ý.
- Payment: PENDING/CONFIRMED/FAILED. Tạo đơn mới chỉ COD; hoàn tất đơn ghi thu COD. Không có endpoint simulate. BANK_TRANSFER/ONLINE chỉ được giữ cho dữ liệu đã có.
- Delivery: nhân viên PUT /api/admin/orders/{id}/delivery {carrier,trackingCode,expectedAt?,note?}; trạng thái từ luồng đơn. Khách đọc delivery trong chi tiết đơn của mình.
- Inventory: import/export dùng lượng tăng/giảm; adjust dùng tồn vật lý mới; PATCH threshold dùng {threshold}. Lịch sử có accountId. Không cho tồn dưới số đang giữ.
- Review: chỉ sau mua hoàn tất, stars 1–5; lưu don_hang_id. Ảnh multipart dùng field file; tối đa 5MB; URL JSON HTTPS hoặc đường dẫn uploads do backend cấp.
- Wishlist: GET phân trang; GET /{id} trả boolean; PUT/DELETE /{id} thêm/xóa theo productId của tài khoản hiện tại.
- Banner: public chỉ HIEN_THI và trong khoảng lịch; link phải là đường dẫn trong website khi ghi qua API. Nội dung hỗ trợ FAQ/CHINH_SACH/GIOI_THIEU/LIEN_HE/TRANG_CHU và slug duy nhất. Frontend render văn bản thuần.
- Thống kê: from/to là ngày UTC gồm hai đầu; doanh thu theo ngày hoàn tất, sau giảm giá và chưa có ship. Theo sản phẩm/danh mục phân bổ giảm theo tỷ lệ dòng. Chi tiêu khách gồm ship. Dashboard đếm ngày đặt và chỉ tính tài khoản KHACH_HANG vào số khách.

## Endpoint và quyền

| Method | Endpoint | Quyền |
|---|---|---|
'''
tail='''

## Status và request mẫu

200 thành công; 201 tạo mới ở các endpoint đã khai báo; 400 request/nghiệp vụ; 401 chưa đăng nhập; 403 không đủ quyền; 404 không có dữ liệu; 409 trùng dữ liệu/tồn kho/chuyển trạng thái; 413 file quá lớn; 500 lỗi nội bộ đã ẩn; 503 email chưa cấu hình.

Schema và status từng operation ở openapi.json hoặc /swagger-ui.html. Import collection + environment trong postman; điền giá trị rỗng bằng tài khoản/ID thật của bạn. Ví dụ request không tự tạo tài khoản và không phải dữ liệu dự phòng của website. starts_at/ends_at dùng ISO-8601 UTC. Chọn trạng thái kế tiếp hợp lệ cho đơn, không gửi ngẫu nhiên enum đầu tiên.

Tạo lại tài liệu: chạy mvn test (xuất target/openapi.json), rồi python scripts/generate_api_docs.py. Script chỉ ghi tài liệu, không gọi API và không thay đổi DB.
'''
(backend/'API.md').write_text(intro+'\n'.join(rows)+tail,encoding='utf-8')
print(f'Generated {len(rows)} endpoint descriptions and {sum(map(len,groups.values()))} Postman requests.')

