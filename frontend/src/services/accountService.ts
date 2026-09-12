import { api } from "./api";
import type { Address } from "../types";
import { type BackendAddress, mapAddress } from "./backendTypes";

const clean = (value: unknown) => String(value ?? "").trim();

const toBody = (address: Address) => ({
  name: clean(address.name),
  phone: clean(address.phone).replace(/[\s.-]/g, ""),
  city: clean(address.city),
  district: clean(address.district),
  ward: clean(address.ward),
  street: clean(address.street),
  defaultAddress: address.defaultAddress ?? true,
});

export const accountService = {
  async addresses(): Promise<Address[]> {
    return (await api.get<BackendAddress[]>("/addresses")).data.map(mapAddress);
  },
  async getAddress(): Promise<Address | null> {
    const list = await this.addresses();
    return list.find((address) => address.defaultAddress) ?? list[0] ?? null;
  },
  async saveAddress(address: Address): Promise<Address> {
    const request = address.id
      ? api.put<BackendAddress>(`/addresses/${encodeURIComponent(address.id)}`, toBody(address))
      : api.post<BackendAddress>("/addresses", toBody(address));
    return mapAddress((await request).data);
  },
  async removeAddress(id: string) {
    await api.delete(`/addresses/${encodeURIComponent(id)}`);
  },
  async setDefault(id: string) {
    await api.put(`/addresses/${encodeURIComponent(id)}/default`);
  },
};
