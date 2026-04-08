#include "Exposure.h"
#include <limits>

Exposure::Exposure(const std::vector<std::vector<unsigned int>>& radiation) {
    radiation_ = radiation;
    rows_ = radiation.size();
    cols_ = radiation[0].size();
}

Exposure::Exposure(const std::vector<std::vector<unsigned int>>& radiation) {
    radiation_ = radiation;
    rows_ = radiation.size();
    cols_ = radiation[0].size();
}



bool Exposure::inBounds(unsigned int row, unsigned int col) {
    return (row < rows_ && col < cols_);
}

bool Exposure::InBounds(unsigned int row, unsigned int col) {
    return (row < rows_ && col < cols_);
}

unsigned int Exposure::radiationAt(unsigned int row, unsigned int col) {
    return radiation_[row][col];
}

unsigned int Exposure::radiationAt(unsigned int row, unsigned int col) {
    return radiation_[row][col];
}


unsigned int Exposure::exposureCost(unsigned int fromRow, unsigned int toRow, unsigned int toCol, unsigned int fromCol) {
    unsigned int val = radiationAt(toRow, toCol);
    if (toRow == fromRow) return val;
    if (toRow < fromRow) return val * 2;
    return val * 3;
}

unsigned int Exposure::exposureCost(unsigned int fromRow, unsigned int toRow, unsigned int toCol, unsigned int fromCol) {
    unsigned int val = radiationAt(toRow, toCol);
    if (toRow == fromRow) return val;
    if (toRow < fromRow) return val * 2;
    return val * 3;
}

std::pair<std::vector<unsigned int>, unsigned int> Exposure::findLowestCost(unsigned int startRow) {
    std::vector<unsigned int> path;
    unsigned int currRow = startRow;
    unsigned int total = 0;
    path.push_back(currRow);
    for (unsigned int col = 1; col < cols_; col++) {
        unsigned int bestRow = currRow;
        unsigned int bestCost = exposureCost(currRow, currRow, col, col - 1);
        if (inBounds(currRow - 1, col)) {
            unsigned int topCost = exposureCost(currRow, currRow - 1, col, col - 1);
            if (topCost < bestCost) {
                bestCost = topCost;
                bestRow = currRow - 1;
            }
        }
        if (inBounds(currRow + 1, col)) {
            unsigned int bottomCost = exposureCost(currRow, currRow + 1, col, col - 1);
            if (bottomCost < bestCost) {
                bestCost = bottomCost;
                bestRow = currRow + 1;
            }
        }
        currRow = bestRow;
        path.push_back(currRow);
        total += bestCost;
    }
    return {path, total};
}

std::pair<std::vector<unsigned int>, unsigned int> Exposure::findLowestCost(unsigned int startRow) {
    std::vector<unsigned int> path;
    unsigned int currRow = startRow;
    unsigned int total = 0;
    path.push_back(currRow);
    for (unsigned int col = 1; col < cols_; col++) {
        unsigned int bestRow = currRow;
        unsigned int bestCost = exposureCost(currRow, currRow, col, col - 1);
        if (inBounds(currRow - 1, col)) {
            unsigned int topCost = exposureCost(currRow, currRow - 1, col, col - 1);
            if (topCost < bestCost) {
                bestCost = topCost;
                bestRow = currRow - 1;
            }
        }
        if (inBounds(currRow + 1, col)) {
            unsigned int bottomCost = exposureCost(currRow, currRow + 1, col, col - 1);
            if (bottomCost < bestCost) {
                bestCost = bottomCost;
                bestRow = currRow + 1;
            }
        }
        currRow = bestRow;
        path.push_back(currRow);
        total += bestCost;
    }
    return {path, total};
}


