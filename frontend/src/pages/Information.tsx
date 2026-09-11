import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { ArrowLeft } from "lucide-react";
import { Breadcrumbs } from "../components/common/Breadcrumbs";
import { EmptyState, ErrorState } from "../components/common/Feedback";
import { contentService, type ContentPage } from "../services/contentService";
import { errorMessage } from "../utils/format";

export default function Information() {
  const { slug = "" } = useParams();
  const [page, setPage] = useState<ContentPage | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [version, setVersion] = useState(0);

  useEffect(() => {
    setLoading(true);
    setError("");
    contentService
      .content(slug)
      .then(setPage)
      .catch((err: unknown) => setError(errorMessage(err)))
      .finally(() => setLoading(false));
  }, [slug, version]);

  if (loading) {
    return (
      <div className="container page">
        <div className="empty-state" role="status">Đang tải nội dung…</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container page">
        <ErrorState message="Không thể tải nội dung." retry={() => setVersion((n) => n + 1)} />
      </div>
    );
  }

  if (!page) {
    return (
      <div className="container page">
        <EmptyState title="Chưa có nội dung" description="Nội dung trang này chưa được cập nhật. Vui lòng quay lại sau.">
          <Link to="/" className="btn btn-primary">
            Về trang chủ
          </Link>
        </EmptyState>
      </div>
    );
  }

  return (
    <div className="container page information-page">
      <Breadcrumbs items={[{ label: page.title }]} />
      <Link to="/" className="link-back">
        <ArrowLeft size={16} /> Về trang chủ
      </Link>
      <article className="information-card">
        <div className="eyebrow">{page.type || "THÔNG TIN"}</div>
        <h1>{page.title}</h1>
        <div className="information-content">
          {page.content
            .split(/\r?\n/)
            .filter(Boolean)
            .map((line, index) => (
              <p key={`${page.id}-${index}`}>{line}</p>
            ))}
        </div>
      </article>
    </div>
  );
}
