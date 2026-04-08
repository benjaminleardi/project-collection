#include "path_image.hpp"

#include <cstdlib>
#include <fstream>
#include <stdexcept>
#include <vector>

constexpr int kRedR = 252;
constexpr int kRedG = 25;
constexpr int kRedB = 63;

constexpr int kGreenR = 31;
constexpr int kGreenG = 253;
constexpr int kGreenB = 13;

PathImage::PathImage(const GrayscaleImage& image,
                     const ElevationDataset& dataset) {
  width_ = image.Width();
  height_ = image.Height();
  path_image_ = image.GetImage();
  BuildPaths(dataset);
  size_t best_index = FindBestPath();
  ColorPaths(best_index);
}
void PathImage::BuildPaths(const ElevationDataset& dataset) {
  for (size_t start = 0; start < height_; start++) {
    Path path(width_, start);
    size_t current_row = start;
    path.SetLoc(0, current_row);
    for (size_t col = 0; col < width_ - 1; col++) {
      int current = dataset.DatumAt(current_row, col);
      int forward = std::abs(dataset.DatumAt(current_row, col + 1) - current);
      int best = forward;
      size_t next_row = current_row;
      if (current_row > 0) {
        int up = std::abs(dataset.DatumAt(current_row - 1, col + 1) - current);
        if (up < best) {
          best = up;
          next_row = current_row - 1;
        }
      }
      if (current_row < height_ - 1) {
        int down =
            std::abs(dataset.DatumAt(current_row + 1, col + 1) - current);
        if ((next_row != current_row && down == best) || down < best) {
          best = down;
          next_row = current_row + 1;
        }
      }
      path.IncEleChange(best);
      current_row = next_row;
      path.SetLoc(col + 1, current_row);
    }
    paths_.push_back(path);
  }
}
size_t PathImage::FindBestPath() const {
  size_t best_index = 0;
  unsigned int best_change = paths_[0].EleChange();
  for (size_t i = 1; i < paths_.size(); i++) {
    if (paths_[i].EleChange() < best_change) {
      best_change = paths_[i].EleChange();
      best_index = i;
    }
  }
  return best_index;
}
void PathImage::ColorPaths(size_t best_index) {
  Color red(kRedR, kRedG, kRedB);
  Color green(kGreenR, kGreenG, kGreenB);

  for (size_t i = 0; i < paths_.size(); i++) {
    const std::vector<size_t>& p = paths_[i].GetPath();

    for (size_t col = 0; col < width_; col++) {
      path_image_[p[col]][col] = red;
    }
  }

  const std::vector<size_t>& best_path = paths_[best_index].GetPath();

  for (size_t col = 0; col < width_; col++) {
    path_image_[best_path[col]][col] = green;
  }
}
size_t PathImage::Width() const { return width_; }
size_t PathImage::Height() const { return height_; }
unsigned int PathImage::MaxColorValue() const { return kMaxColorValue; }
const std::vector<Path>& PathImage::Paths() const { return paths_; }
const std::vector<std::vector<Color>>& PathImage::GetPathImage() const {
  return path_image_;
}
void PathImage::ToPpm(const std::string& name) const {
  std::ofstream file(name);
  if (!file.is_open()) {
    throw std::runtime_error("Could not open");
  }

  file << "P3\n";
  file << width_ << " " << height_ << "\n";
  file << kMaxColorValue << "\n";

  for (size_t i = 0; i < height_; i++) {
    for (size_t j = 0; j < width_; j++) {
      Color color = path_image_[i][j];
      file << color.Red() << " " << color.Green() << " " << color.Blue() << " ";
    }
    file << "\n";
  }
  file << "\n";
}