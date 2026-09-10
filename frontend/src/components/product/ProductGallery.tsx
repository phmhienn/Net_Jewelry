import { useState } from "react";
export function ProductGallery({
  images,
  name,
}: {
  images: string[];
  name: string;
}) {
  const [index, setIndex] = useState(0);
  return (
    <div className="gallery">
      <div className="gallery-main">
        <img
          src={images[index] ?? images[0]}
          alt={`${name} — ảnh ${index + 1}`}
          width="800"
          height="900"
        />
      </div>
      <div className="gallery-thumbnails">
        {images.map((image, i) => (
          <button
            key={image}
            className={index === i ? "active" : ""}
            onClick={() => setIndex(i)}
            aria-label={`Xem ảnh ${i + 1}`}
            aria-pressed={index === i}
          >
            <img src={image} alt="" loading="lazy" width="100" height="110" />
          </button>
        ))}
      </div>
    </div>
  );
}
