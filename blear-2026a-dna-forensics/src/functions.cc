#include "functions.hpp"

#include <fstream>
#include <iostream>
#include <stdexcept>

std::vector<std::string> GetSTRs(const std::string& header) {
  std::vector<std::string> all_columns = utilities::GetSubstrs(header, ',');
  std::vector<std::string> strings;
  for (size_t i = 1; i < all_columns.size(); i++) {
    strings.push_back(all_columns[i]);
  }

  return strings;
}

int LongestRun(const std::string& dna_sequence, const std::string& str) {
  int max = 0;
  size_t i = 0;

  while (i < dna_sequence.size()) {
    int curr = 0;
    while (dna_sequence.size() >= i + str.size() &&
           dna_sequence.substr(i, str.size()) == str) {
      curr++;
      i += str.size();
    }
    if (curr == 0) {
      i++;
    } else {
      if (curr > max) {
        max = curr;
      }
    }
  }
  return max;
}

std::vector<int> GetSequenceProfile(const std::string& dna_sequence,
                                    const std::vector<std::string>& strs) {
  std::vector<int> profile;

  for (std::string str : strs) {
    int c = LongestRun(dna_sequence, str);
    profile.push_back(c);
  }
  return profile;
}

std::pair<std::string, std::vector<int>> ParseRow(const std::string& row) {
  std::vector<std::string> keys = utilities::GetSubstrs(row, ',');
  std::string name = keys[0];
  std::vector<int> numbers;

  for (size_t i = 1; i < keys.size(); i++) {
    int new_int = std::stoi(keys[i]);
    numbers.push_back(new_int);
  }

  return {name, numbers};
}

bool IsMatch(const std::vector<int>& sequence_profile,
             const std::vector<int>& person_profile) {
  for (size_t i = 0; i < sequence_profile.size(); i++) {
    if (sequence_profile[i] != person_profile[i]) {
      return false;
    }
  }
  return true;
}

std::string ProfileDNA(const std::string& dna_database,
                       const std::string& dna_sequence) {
  std::ifstream ifs{dna_database};
  std::string line;
  std::getline(ifs, line);
  std::vector<std::string> strs = GetSTRs(line);

  std::vector<int> new_profile = GetSequenceProfile(dna_sequence, strs);

  std::string match;
  int count = 0;

  for (std::string row; std::getline(ifs, row); row = "") {
    std::pair<std::string, std::vector<int>> person = ParseRow(row);
    if (IsMatch(new_profile, person.second)) {
      match = person.first;
      count++;
    }
  }

  if (count == 1) {
    return match;
  }

  return "No match";
}
