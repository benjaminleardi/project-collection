#include "db_table.hpp"

#include <initializer_list>
#include <iostream>
#include <map>
#include <string>
#include <utility>
#include <vector>

void DbTable::AddColumn(const std::pair<std::string, DataType>& col_desc) {
  unsigned int original_size = col_descs_.size();
  if (original_size == row_col_capacity_) {
    unsigned int new_capacity = row_col_capacity_ * 2;

    for (auto& [row_id, row_array] : rows_) {
      void** new_row = new void*[new_capacity];
      for (size_t i = 0; i < original_size; i++) {
        new_row[i] = row_array[i];
      }
      delete[] row_array;
      rows_[row_id] = new_row;
    }
    row_col_capacity_ = new_capacity;
  }
  col_descs_.push_back(col_desc);
  DataType type = col_desc.second;
  for (auto& [row_id, row_array] : rows_) {
    if (type == DataType::kString) {
      row_array[original_size] = static_cast<void*>(new std::string(""));
    } else if (type == DataType::kInt) {
      row_array[original_size] = static_cast<void*>(new int(0));
    } else if (type == DataType::kDouble) {
      row_array[original_size] = static_cast<void*>(new double(0.0));
    }
  }
}

void DbTable::AddRow(const std::initializer_list<std::string>& col_data) {
  if (col_descs_.size() != col_data.size()) {
    throw std::invalid_argument("Invalid size");
  }
  void** row = new void*[row_col_capacity_];
  int i = 0;
  for (const std::string& value : col_data) {
    DataType type = col_descs_[i].second;
    if (type == DataType::kString) {
      row[i] = new std::string(value);
    } else if (type == DataType::kInt) {
      row[i] = new int(std::stoi(value));
    } else if (type == DataType::kDouble) {
      row[i] = new double(std::stod(value));
    }
    i++;
  }
  rows_[next_unique_id_] = row;
  next_unique_id_++;
}

std::ostream& operator<<(std::ostream& os, const DbTable& table) {
  for (size_t i = 0; i < table.col_descs_.size(); i++) {
    os << table.col_descs_[i].first << "(";
    if (table.col_descs_[i].second == DataType::kString) {
      os << "std::string";
    } else if (table.col_descs_[i].second == DataType::kInt) {
      os << "int";
    } else if (table.col_descs_[i].second == DataType::kDouble) {
      os << "double";
    }
    os << ")";
    if (i < table.col_descs_.size() - 1) {
      os << ", ";
    }
  }
  os << "\n";
  for (const auto& [row_id, row] : table.rows_) {
    for (size_t i = 0; i < table.col_descs_.size(); i++) {
      if (row[i] == nullptr) {
        os << "NULL";
      } else {
        DataType type = table.col_descs_[i].second;
        if (type == DataType::kString) {
          os << *static_cast<std::string*>(row[i]);
        } else if (type == DataType::kInt) {
          os << *static_cast<int*>(row[i]);
        } else if (type == DataType::kDouble) {
          os << *static_cast<double*>(row[i]);
        }
      }
      if (i < table.col_descs_.size() - 1) {
        os << " ";
      }
    }
    os << "\n";
  }
  return os;
}

void DbTable::DeleteRowById(unsigned int id) {
  if (!rows_.contains(id)) {
    throw std::out_of_range("Id does not exist");
  }
  void** row = rows_.at(id);

  for (size_t col_idx = 0; col_idx < col_descs_.size(); col_idx++) {
    DataType type = col_descs_[col_idx].second;
    if (type == DataType::kString) {
      delete static_cast<std::string*>(row[col_idx]);
    } else if (type == DataType::kInt) {
      delete static_cast<int*>(row[col_idx]);
    } else if (type == DataType::kDouble) {
      delete static_cast<double*>(row[col_idx]);
    }
  }
  delete[] row;
  rows_.erase(id);
}

void DbTable::DeleteColumnByIdx(unsigned int col_idx) {
  if (col_idx >= col_descs_.size()) {
    throw std::out_of_range("Not in range");
  }
  if (col_descs_.size() == 1 && !rows_.empty()) {
    throw std::runtime_error("Can't delete the last column");
  }
  for (auto& [row_id, row] : rows_) {
    DataType type = col_descs_[col_idx].second;
    if (type == DataType::kString) {
      delete static_cast<std::string*>(row[col_idx]);
    } else if (type == DataType::kInt) {
      delete static_cast<int*>(row[col_idx]);
    } else if (type == DataType::kDouble) {
      delete static_cast<double*>(row[col_idx]);
    }
    for (size_t i = col_idx; i < col_descs_.size() - 1; i++) {
      row[i] = row[i + 1];
    }
  }
  col_descs_.erase(col_descs_.begin() + col_idx);
}

DbTable& DbTable::operator=(const DbTable& rhs) {
  if (this == &rhs) return *this;
  for (unsigned int row_id = 0; row_id < next_unique_id_; row_id++) {
    if (rows_.contains(row_id)) {
      DeleteRowById(row_id);
    }
  }
  col_descs_ = rhs.col_descs_;
  row_col_capacity_ = rhs.row_col_capacity_;
  next_unique_id_ = 0;

  for (const auto& [row_id, row] : rhs.rows_) {
    void** new_row = new void*[row_col_capacity_];
    for (size_t i = 0; i < col_descs_.size(); i++) {
      DataType type = col_descs_[i].second;
      if (type == DataType::kString) {
        new_row[i] = new std::string(*static_cast<std::string*>(row[i]));
      } else if (type == DataType::kInt) {
        new_row[i] = new int(*static_cast<int*>(row[i]));
      } else if (type == DataType::kDouble) {
        new_row[i] = new double(*static_cast<double*>(row[i]));
      }
    }
    rows_[next_unique_id_++] = new_row;
  }
  return *this;
}

DbTable::~DbTable() {
  for (auto& [row_id, row_array] : rows_) {
    for (unsigned int i = 0; i < col_descs_.size(); i++) {
      if (row_array[i] != nullptr) {
        if (col_descs_[i].second == DataType::kString) {
          delete static_cast<std::string*>(row_array[i]);
        } else if (col_descs_[i].second == DataType::kInt) {
          delete static_cast<int*>(row_array[i]);
        } else if (col_descs_[i].second == DataType::kDouble) {
          delete static_cast<double*>(row_array[i]);
        }
      }
    }
    delete[] row_array;
  }
  rows_.clear();
}

DbTable::DbTable(const DbTable& rhs) {
  next_unique_id_ = 0;
  row_col_capacity_ = rhs.row_col_capacity_;
  col_descs_ = rhs.col_descs_;
  for (const auto& [row_id, row] : rhs.rows_) {
    void** new_row = new void*[row_col_capacity_];
    for (size_t i = 0; i < col_descs_.size(); i++) {
      DataType type = col_descs_[i].second;
      if (type == DataType::kString) {
        new_row[i] = new std::string(*static_cast<std::string*>(row[i]));
      } else if (type == DataType::kInt) {
        new_row[i] = new int(*static_cast<int*>(row[i]));
      } else if (type == DataType::kDouble) {
        new_row[i] = new double(*static_cast<double*>(row[i]));
      }
    }
    rows_[next_unique_id_++] = new_row;
  }
}