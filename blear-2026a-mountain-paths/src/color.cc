#include "color.hpp"

#include <stdexcept>

Color::Color(int r, int g, int b): red_(r), green_(g), blue_(b) {
  if (red_ > kColorValueMax || red_ < kColorValueMin) {
    throw std::invalid_argument("Color is not valid");
  }
  if (blue_ > kColorValueMax || blue_ < kColorValueMin) {
    throw std::invalid_argument("Color is not valid");
  }
  if (green_ > kColorValueMax || green_ < kColorValueMin) {
    throw std::invalid_argument("Color is not valid");
  }
}

// do not modify
bool operator==(const Color& rhs, const Color& lhs) {
  return (rhs.Red() == lhs.Red() && rhs.Green() == lhs.Green() &&
          rhs.Blue() == lhs.Blue());
}
