import { useState } from "react";

export function ProductGallery({ images, name }: { images: string[]; name: string }) {
  const [index, setIndex] = useState(0);
  const currentImage = images[index] ?? images[0];
  return (
    <div className="gallery">
      <div className="gallery-main">
        {currentImage ? (
          <img src={currentImage} alt={`${name} — ảnh ${index + 1}`} width="800" height="900" />
        ) : (
          <div className="gallery-placeholder" role="img" aria-label={`Chưa có ảnh cho ${name}`}>
            Chưa có ảnh sản phẩm
          </div>
        )}
      </div>
      {images.length > 1 && (
        <div className="gallery-thumbnails">
          {images.map((image, i) => (
            <button
              key={`${image}-${i}`}
              className={index === i ? "active" : ""}
              onClick={() => setIndex(i)}
              aria-label={`Xem ảnh ${i + 1}`}
              aria-pressed={index === i}
            >
              <img src={image} alt="" loading="lazy" width="100" height="110" />
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
