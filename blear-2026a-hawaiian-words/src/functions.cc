#include "functions.hpp"

#include <cctype>
#include <iostream>
#include <string>

/**
 * TODO: Implement this function
 * @param word word to change to lowercase
 * @return result of changing every character in word to lowercase
 */
std::string StringToLower(const std::string& word) {
  std::string word_lowercase = "";

  for (unsigned int i = 0; i < word.size(); i++) {
    unsigned char c = static_cast<unsigned char>(word[i]);
    if (isupper(c)) {
      word_lowercase += static_cast<char>(std::tolower(c));
    } else {
      word_lowercase += c;
    }
  }

  std::cout << "Input to StringToLower() : " << word << std::endl;
  return word_lowercase;
}

/**
 * TODO: Implement this function
 * @param v1 first character in potential vowel group
 * @param v2 second character in potential vowel group
 * @return true if v1 and v2 form a valid vowel group
 */
bool IsVowelGroup(const char& v1, const char& v2) {
  std::string group = {v1, v2};
  bool isVowelGroup;
  if (group == "ai" || group == "ae" || group == "ao" || group == "au" ||
      group == "ei" || group == "eu" || group == "iu" || group == "oi" ||
      group == "ou" || group == "ui") {
    isVowelGroup = true;
  } else {
    isVowelGroup = false;
  }

  std::cout << "Input to IsVowelGroup() : \"" << v1 << "\" \"" << v2 << "\""
            << std::endl;
  return isVowelGroup;
}

/**
 * TODO: Complete this function
 * @param v potential vowel character
 * @return true if v is a vowel
 */
bool IsVowel(const char& v) {
  bool isVowel;
  if (v == 'a' || v == 'e' || v == 'i' || v == 'o' || v == 'u') {
    isVowel = true;
  } else {
    isVowel = false;
  }

  std::cout << "Input to IsVowel() : \"" << v << "\"" << std::endl;
  return isVowel;
}

/**
 * TODO: Complete this function
 * @param c potential consonant character
 * @return true if c is a consonant
 */
bool IsConsonant(const char& c) {
  bool isConsonant;
  if (c == 'p' || c == 'k' || c == 'h' || c == 'l' || c == 'm' || c == 'n' ||
      c == 'w') {
    isConsonant = true;
  } else {
    isConsonant = false;
  }

  std::cout << "Input to IsConsonant() : \"" << c << "\"" << std::endl;
  return isConsonant;
}

/**
 * TODO: Implement this function
 * @param c character to check validity of
 * @return true if input c is a valid character in the Hawaiian language, false
 * otherwise
 */
bool IsValidCharacter(const char& c) {
  bool isValidCharacter;
  if (IsVowel(c) || IsConsonant(c) || c == ' ' || c == '\'') {
    isValidCharacter = true;
  } else {
    isValidCharacter = false;
  }

  std::cout << "Input to IsValidCharacter() : \"" << c << "\"" << std::endl;
  return isValidCharacter;
}

/**
 * TODO: Implement this function
 * @param word word to check validity of
 * @return true if every character in word is a valid character in the Hawaiian
 * language, false otherwise
 */
bool IsValidWord(const std::string& word) {
  bool isValidWord = true;
  for (size_t i = 0; i < word.size(); i++) {
    if (IsValidCharacter(word[i]) == false) {
      isValidWord = false;
    }
  }

  std::cout << "Input to IsValidWord() : " << word << std::endl;
  return isValidWord;
}

/**
 * TODO: Implement this function
 * @param c consonant to get pronunciation of
 * @param prev character before c, used for determining w translation
 *    set to null character as default if no value is passed in
 * @return pronunciation of the consonant c as a char
 */
char ConsonantPronunciation(const char& c, const char& prev) {
  if (c == 'p' || c == 'k' || c == 'h' || c == 'l' || c == 'm' || c == 'n' ||
      c == '\'') {
    return c;
  } else if (c == 'w') {
    if (prev == '\0' || prev == 'a' || prev == 'u' || prev == 'o' ||
        prev == ' ') {
      return 'w';
    } else if (prev == 'i' || prev == 'e') {
      return 'v';
    }
  }

  std::cout << "Input to ConsonantPronunciation() : \"" << c << "\" \"" << prev
            << "\"" << std::endl;
  return '\0';
}

/**
 * TODO: Implement this function
 * @param v1 first vowel in a vowel grouping
 * @param v2 second vowel in a vowel grouping
 * @return the pronunciation of the vowel grouping made up of v1 and v2
 * as a string
 */
std::string VowelGroupPronunciation(const char& v1, const char& v2) {
  if ((v1 == 'a' && v2 == 'i') || (v1 == 'a' && v2 == 'e')) {
    return "eye";
  } else if ((v1 == 'a' && v2 == 'o') || (v1 == 'a' && v2 == 'u')) {
    return "ow";
  } else if ((v1 == 'e' && v2 == 'i')) {
    return "ay";
  } else if ((v1 == 'e' && v2 == 'u')) {
    return "eh-oo";
  } else if ((v1 == 'i' && v2 == 'u')) {
    return "ew";
  } else if ((v1 == 'o' && v2 == 'i')) {
    return "oy";
  } else if ((v1 == 'o' && v2 == 'u')) {
    return "ow";
  } else if ((v1 == 'u' && v2 == 'i')) {
    return "ooey";
  }

  std::cout << "Input to VowelGroupPronunciation() : \"" << v1 << "\" \"" << v2
            << "\"" << std::endl;
  return "";
}

/**
 * TODO: Implement this function
 * @param v single vowel to get pronunciation of
 * @return the pronunciation of v as a string
 */
std::string SingleVowelPronunciation(const char& v) {
  if (v == 'a') {
    return "ah";
  } else if (v == 'e') {
    return "eh";
  } else if (v == 'i') {
    return "ee";
  } else if (v == 'o') {
    return "oh";
  } else if (v == 'u') {
    return "oo";
  }
  std::cout << "Input to SingleVowelPronunciation() : \"" << v << "\""
            << std::endl;
  return "";
}

/**
 * TODO: Implement this function
 * @param prev first character in set of three passed to function
 * @param curr second character in set of three passed to function
 * @param next third character in set of three passed to function
 * @return pronunciation of curr using next and prev as needed to determine
 * result
 */
std::string ProcessCharacter(const char& prev,
                             const char& curr,
                             const char& next) {
  if (!(IsValidCharacter(curr))) {
    return "";
  }

  if (curr == '\'') {
    return "\'";
  }

  if (curr == ' ') {
    return " ";
  }

  if (IsConsonant(curr)) {
    char current_consonat = ConsonantPronunciation(curr, prev);
    return std::string(1, current_consonat);
  }
  
  if (IsVowel(curr)) {

    if (IsVowelGroup(prev, curr)) {
      return "";
    }

    if (IsVowel(curr) && IsVowelGroup(curr, next)) {
      return VowelGroupPronunciation(curr, next);
    } else {
      return SingleVowelPronunciation(curr);
    }
  }

  std::cout << "Input to ProcessCharacter() : \"" << prev << "\" \"" << curr
            << "\" \"" << next << "\"" << std::endl;
  return "";
}

/**
 * TODO: Implement this function
 * @param word string to get pronunciation of
 * @return pronunciation of word
 */
std::string Pronunciation(const std::string& word) {
  std::string pronunciation = "";
  for (size_t i = 0; i < word.size(); i++) {
    char previous = (i == 0) ? '\0' : word[i - 1];
    char current = word[i];
    char next = (i == word.length() - 1) ? '\0' : word[i + 1];
    pronunciation += ProcessCharacter(previous, current, next);

    if (i < word.length() - 1) {
      char next = word[i + 1];

      if (IsVowel(current)) {
        bool group = IsVowelGroup(current, next);

        if (next != ' ' && next != '\'' && !group && next != '\0') {
          pronunciation += "-";
        }
      }
    }
  }

  std::cout << "Input to Pronunciation() : " << word << std::endl;
  return pronunciation;
}

/**
 * TODO: Implement this function
 * @param hawaiian_word word to get pronunciation of
 * @return the pronunciation of hawaiian_word as a string if valid
 *  and throws an invalid_argument error if not valid
 */
std::string GetPronunciation(const std::string& hawaiian_word) {
  std::string lowercase = StringToLower(hawaiian_word);
  if (!IsValidWord(lowercase)) {
    throw std::invalid_argument("This is not a word");
  }
  std::cout << "Input to GetPronunciation() : " << hawaiian_word << std::endl;
  return Pronunciation(lowercase);
}

std::string ConvertWord(std::string word) {
  std::string pre = prefix(word);

  if (pre.size() == 0) {
    return word + "yay";
  }

  std::string rest = ""; 
  for (int i = pre.length(); i < word.legnth(); i++) {
    rest += word[i]; 
  }
  
  return rest + pre + "ay";
}

std::string Sentence(std::string str) {
  std::string result = "";
  std::string word = "";

  for (int i = 0; i < str.length(); i++) {
    if (str[i] == ' ') {
      result += convertWord(word) + ' ';
      word = "";
    } else {
      word += str[i];
    }
  }

  result += ConvertWord(word);

  return result;
}

std::string GetPigLatin(std::string str) {
  std::string result = toLower(str);

  for (int i = 0; i < result.length(); i++) {
    if (!std::isalpha(result[i]) && result[i] != ' ') {
      throw std::invalid_argument("Argument must only contain letters and spaces");
    }
  }

  return Sentence(result);
}



std::string ConvertWord(std::string word) {
  std::string pre = prefix(word);

  if (pre.size() == 0) {
    return pre + "yay";
  }

  std::string rest = "";

  for (int i = pre.length(); i < word.size(); i++) {
     rest += word[i];
  }
  
  return rest + pre + "ay";

}

std::string Sentence(std::string str) {
  std::string result = "";
  std::string word = "";

  for (int i = 0; i < str.size(); i++) {
    if (str[i] == ' ') {
      result += ConvertWord(word) + ' ';
      word += ' '
    } else {
      word += str[i];
    }
  }
  
  result += ConvertWord(word)
  
  return result;

}

std::string GetPigLating(std::string str) {
  std::string result = toLower(str);

  for (int i = 0; i < result.length(); i++) {
    if (!std::isalpha(result[i]) && result[i] != ' ') {
      throw std::invalid_argument("Sentence can only contain letters and spaces");
    }
  }

  return sentence(result);
}


std::string ConvertWord(std::string word) {
  std::string pre = prefix(word);

  if (pre.length() == 0) {
    return pre + "yay";
  }

  std::string rest = "";
  for (int i = pre.length(); i < word.size(); i++) {
    rest = word[i];
  }

  return rest + pre + "ay";
}

std::string Sentence(std::string str) {
  std::string word = "";
  std::string result = "";

  for (int i = 0; i < str.size(); i++) {
    if (str[i] == ' ') {
      result = ConvertWord(str) + ' ';
      word = ""
    } else {
      word += str[i];
    }
  }

  result += ConvertWord(word);

  return result;
}

std::string GetPigLating(std::string str) {
  std::string result = toLower(str);

  for (int i = 0; i < result.length(); i++) {
    if (!std::isalpha(result[i]) && result[i] != ' ') {
      throw std::invalid_argument("Sentence can only be letters and spaces");
    }
  }

  return sentence(result);

}

std::string ConvertWord(std::stirng word) {
  std::string pre = prefix(word);

  if (pre == 0) {
    return pre + "yay";
  }

  std::string rest = "";
  for (int i = pre.length(); i < word.length(); i++) {
    rest += word[i];
  }

  return rest + pre + "ay";
}

std::string Sentence(std::string str) {
  std::string word = ""; 
  std::string result = "";

  for (int i = 0; i < (int)str.size(); i++) {
    if (str[i] == ' ') {
      result += ConvertWord(word) + ' ';
      word = ""
    } else {
      word += str[i]
    }
  }

  result += ConvertWord(word);

  return result;
}

std::string GetPigLatin(std::string str) {
  std::string result = stringToLower(str);

  for (int i = 0; i < result.length(); i++) {
    if (!std::isalpha(result[i]) && result[i] != ' ') {
      throw std::invalid_argument("Sentence can only be letters and spaces");
    }
  }

  return sentence(result);
}

std::string ConvertWord(std::string word) {
  std::string pre = prefix(word);

  if (pre.size() == 0) {
    return word + "yay";
  }
  
  std::string rest = "";
  for (int i = pre.size(); i < (int)word.size(); i++) {
    rest += word[i];
  }

  return rest + pre + "ay";
}

std::string Sentence(std::string str) {
  std::string word = "";
  std::string result = "";

  for (int i = 0; i < (int)str.size(); i++) {
    if (word[i] == ' ') {
      result += ConvertWord(word) + ' ';
      word = ""
    } else {
      word += str[i];
    }
  }

  result += ConvertWord(word);
  return result;
}

std::string GetPigLatin(std::string str) {
  std::string result = stringtolower(str);

  for (int i = 0; i < result.size(); i++) {
    if (!std::isalpha(result[i]) && result[i] != ' ') {
      throw std::invalid_argument("Sentence can only be letters and spaces");
    }
  }

  return Sentence(result);
}