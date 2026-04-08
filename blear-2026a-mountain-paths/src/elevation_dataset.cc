#include "elevation_dataset.hpp"

#include <fstream>
#include <limits>
#include <stdexcept>
#include <string>
#include <vector>

ElevationDataset::ElevationDataset(const std::string& filename,
                                   size_t width,
                                   size_t height) {
  width_ = width;
  height_ = height;
  max_ele_ = std::numeric_limits<int>::min();
  min_ele_ = std::numeric_limits<int>::max();
  data_ = std::vector<std::vector<int>>(height_, std::vector<int>(width_));
  std::ifstream input(filename);
  if (!input.is_open()) {
    throw std::runtime_error("File cannot be opened");
  }

  for (size_t i = 0; i < height_; i++) {
    for (size_t j = 0; j < width_; j++) {
      int file_value = 0;
      if (!(input >> file_value)) {
        throw std::runtime_error("File is too little");
      }

      data_[i][j] = file_value;

      if (file_value > max_ele_) {
        max_ele_ = file_value;
      }
      if (file_value < min_ele_) {
        min_ele_ = file_value;
      }
    }
  }

  int extra_data = 0;
  if (input >> extra_data) {
    throw std::runtime_error("File is too big");
  }
}

size_t ElevationDataset::Width() const { return width_; }

size_t ElevationDataset::Height() const { return height_; }

int ElevationDataset::MaxEle() const { return max_ele_; }

int ElevationDataset::MinEle() const { return min_ele_; }

int ElevationDataset::DatumAt(size_t row, size_t col) const {
  return data_[row][col];
}

const std::vector<std::vector<int>>& ElevationDataset::GetData() const {
  return data_;
}