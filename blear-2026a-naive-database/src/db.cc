#include "db.hpp"

#include <iostream>

#include "db_table.hpp"

void Database::CreateTable(const std::string& table_name) {
  if (tables_.contains(table_name)) {
    throw std::runtime_error("table already exists");
  }
  tables_[table_name] = new DbTable();
}

void Database::DropTable(const std::string& table_name) {
  auto it = tables_.find(table_name);
  if (it == tables_.end()) {
    throw std::runtime_error("table not found");
  }
  delete it->second;
  tables_.erase(it);
}
DbTable& Database::GetTable(const std::string& table_name) {
  auto it = tables_.find(table_name);
  if (it == tables_.end()) {
    throw std::runtime_error("table not found");
  }
  return *(it->second);
}

Database::Database(const Database& rhs) {
  for (const auto& [table_name, table_ptr] : rhs.tables_) {
    tables_[table_name] = new DbTable(*table_ptr);
  }
}
Database& Database::operator=(const Database& rhs) {
  if (this == &rhs) return *this;
  for (auto& [name, table_ptr] : tables_) {
    delete table_ptr;
  }
  tables_.clear();
  for (const auto& [table_name, table_ptr] : rhs.tables_) {
    tables_[table_name] = new DbTable(*table_ptr);
  }

  return *this;
}
Database::~Database() {
  for (auto& [name, table_ptr] : tables_) {
    delete table_ptr;
  }
  tables_.clear();
}

std::ostream& operator<<(std::ostream& os, const Database& db) {
  for (const auto& [table_name, table_ptr] : db.tables_) {
    os << "Table:" << table_name << "\n";
    os << *table_ptr << "\n";
  }
  return os;
}