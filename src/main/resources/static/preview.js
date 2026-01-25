function initImagePreview(inputSelector, previewSelector) {
  const fileInput = document.querySelector(inputSelector);
  const preview = document.querySelector(previewSelector);

  if (!fileInput || !preview) return;

  fileInput.addEventListener("change", (event) => {
    const file = event.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (e) => {
        preview.src = e.target.result;
      };
      reader.readAsDataURL(file);
    }
  });
}

// 페이지 로드 후 자동 초기화
document.addEventListener("DOMContentLoaded", () => {
  // 마이페이지 (프로필 수정)
  initImagePreview("input[name='image']", "#profile-preview");

  // 게시글 작성 페이지 (필요 시)
  initImagePreview("input[name='image']", "#article-image-preview");
});
