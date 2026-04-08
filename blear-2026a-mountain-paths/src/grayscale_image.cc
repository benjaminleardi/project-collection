#include "grayscale_image.hpp"

#include <cmath>
#include <fstream>
#include <stdexcept>
#include <vector>

GrayscaleImage::GrayscaleImage(const ElevationDataset& dataset) {
  width_ = dataset.Width();
  height_ = dataset.Height();
  int min = dataset.MinEle();
  int max = dataset.MaxEle();
  image_ = std::vector<std::vector<Color>>(height_, std::vector<Color>(width_));

  for (size_t i = 0; i < height_; i++) {
    for (size_t j = 0; j < width_; j++) {
      int shade = 0;
      int value = dataset.DatumAt(i, j);
      if (min == max) {
        shade = 0;
      } else {
        shade = static_cast<int>(
            std::round((double)(value - min) / (max - min) * kMaxColorValue));
      }
      image_[i][j] = Color(shade, shade, shade);
    }
  }
}

GrayscaleImage::GrayscaleImage(const std::string& filename,
                               size_t width,
                               size_t height):
    GrayscaleImage(ElevationDataset(filename, width, height)) {}
size_t GrayscaleImage::Width() const { return width_; }
size_t GrayscaleImage::Height() const { return height_; }
unsigned int GrayscaleImage::MaxColorValue() const { return kMaxColorValue; }
const Color& GrayscaleImage::ColorAt(int row, int col) const {
  return image_[row][col];
}
const std::vector<std::vector<Color>>& GrayscaleImage::GetImage() const {
  return image_;
}
void GrayscaleImage::ToPpm(const std::string& name) const {
  std::ofstream file(name);
  if (!file.is_open()) {
    throw std::runtime_error("Could not open");
  }

  file << "P3\n";
  file << width_ << " " << height_ << "\n";
  file << kMaxColorValue << "\n";

  for (size_t i = 0; i < height_; i++) {
    for (size_t j = 0; j < width_; j++) {
      Color color = image_[i][j];
      file << color.Red() << " " << color.Green() << " " << color.Blue() << " ";
    }
    file << "\n";
  }
  file << "\n";
}