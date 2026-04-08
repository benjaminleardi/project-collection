#ifndef FUNCTIONS_HPP
#define FUNCTIONS_HPP

#include <string>

#include "utilities.hpp"

std::string ProfileDNA(const std::string& dna_database,
                       const std::string& dna_sequence);

std::vector<std::string> GetSTRs(const std::string& header);

int LongestRun(const std::string& dna_squence, const std::string& str);

std::vector<int> GetSequenceProfile(const std::string& dna_sequence,
                                    const std::vector<std::string>& strs);

std::pair<std::string, std::vector<int>> ParseRow(const std::string& row);

bool IsMatch(const std::vector<int>& sequence_profile,
             const std::vector<int>& person_profile);

#endif