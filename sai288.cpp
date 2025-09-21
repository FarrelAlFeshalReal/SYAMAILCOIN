#include <iostream>
#include <vector>
#include <cmath>
#include <cstdint>
#include <iomanip>
#include <fstream>
#include <ctime>

uint64_t factorial(int n) {
    uint64_t result = 1;
    for (int i = 2; i <= n; ++i) result *= i;
    return result;
}

double exponomial_constant(int i, double gamma, double R, double tau, std::vector<double> S_list, double phi) {
    double exp_factor = pow(gamma, i / R);
    double ssum = 0.0;
    for (int j = 0; j <= std::min(i, (int)S_list.size() - 1); ++j)
        ssum += S_list[j] * pow(phi, j);
    return exp_factor * tau * ssum;
}

double proof_of_exponomial(int n, int r, int delta_n, int delta_r) {
    std::cout << "Calculating Delta Maths for Proof of Exponomial..." << std::endl;
    std::cout << "Parameters: n=" << n << ", r=" << r << ", delta_n=" << delta_n << ", delta_r=" << delta_r << std::endl;
    try {
        if (n > 20 || delta_n > 20) {
            double term1 = n * log(n) - n + 0.5 * log(2 * M_PI * n) -
                           (r * log(r) - r + 0.5 * log(2 * M_PI * r)) -
                           ((n - r) * log(n - r) - (n - r) + 0.5 * log(2 * M_PI * (n - r)));
            double term2 = delta_n * log(delta_n) - delta_n + 0.5 * log(2 * M_PI * delta_n) -
                           (delta_r * log(delta_r) - delta_r + 0.5 * log(2 * M_PI * delta_r)) -
                           ((delta_n - delta_r) * log(delta_n - delta_r) - (delta_n - delta_r) + 0.5 * log(2 * M_PI * (delta_n - delta_r)));
            return fabs(exp(term1) - exp(term2));
        } else {
            double term1 = factorial(n) / (factorial(r) * factorial(n - r));
            double term2 = factorial(delta_n) / (factorial(delta_r) * factorial(delta_n - delta_r));
            return fabs(term1 - term2);
        }
    } catch (...) {
        std::cout << "Delta Maths error" << std::endl;
        return 0.0;
    }
}

std::vector<uint8_t> sai288_hash(const std::vector<uint8_t>& data) {
    std::vector<uint32_t> S = {0x243F6A88, 0x85A308D3, 0x13198A2E, 0x03707344,
                               0xA4093822, 0x299F31D0, 0x082EFA98, 0xEC4E6C89, 0x452821E6};
    double gamma = 1.05, R = 10.0, tau = 0.5, phi = 0.9;

    size_t padding_length = 72 - (data.size() % 72);
    if (padding_length == 72) padding_length = 0;
    std::vector<uint8_t> padded_data = data;
    if (padding_length > 0) {
        padded_data.push_back(0x80);
        padded_data.insert(padded_data.end(), padding_length - 1, 0x00);
    }

    for (size_t block_start = 0; block_start < padded_data.size(); block_start += 72) {
        std::vector<uint32_t> M(18, 0);
        for (int i = 0; i < 18; ++i) {
            size_t offset = block_start + i * 4;
            M[i] = (static_cast<uint32_t>(padded_data[offset]) << 24) |
                   (static_cast<uint32_t>(padded_data[offset + 1]) << 16) |
                   (static_cast<uint32_t>(padded_data[offset + 2]) << 8) |
                   static_cast<uint32_t>(padded_data[offset + 3]);
        }

        for (int t = 0; t < 64; ++t) {
            uint32_t f1 = (S[(t + 1) % 9] ^ M[t % 18]) +
                          static_cast<uint32_t>(std::pow(gamma, t / R) * tau) ^
                          ((S[(t + 4) % 9] << static_cast<int>(phi * t) % 32) | (S[(t + 4) % 9] >> (32 - static_cast<int>(phi * t) % 32)));
            uint32_t f2 = (S[(t + 5) % 9] + M[static_cast<int>(t * phi) % 18]) ^
                          ((S[(t + 7) % 9] >> (t % 29)) | (S[(t + 7) % 9] << (32 - (t % 29))));
            uint32_t T = S[t % 9];
            S[t % 9] = f1 + f2 + T;
        }

        for (int i = 0; i < 9; ++i) S[i] ^= M[i % 18];
    }

    std::vector<uint8_t> out(36, 0);
    for (int i = 0; i < 9; ++i) {
        out[i * 4] = (S[i] >> 24) & 0xFF;
        out[i * 4 + 1] = (S[i] >> 16) & 0xFF;
        out[i * 4 + 2] = (S[i] >> 8) & 0xFF;
        out[i * 4 + 3] = S[i] & 0xFF;
    }
    return out;
}

int main() {
    std::vector<double> S_list(10, 1.0);
    int block_index = 0;
    std::vector<uint8_t> raw_bytes(16);
    for (int i = 0; i < raw_bytes.size(); i++) raw_bytes[i] = (i + block_index + 1) % 256;

    auto hash = sai288_hash(raw_bytes);
    std::cout << "SAI288 Hash: ";
    for (auto b : hash) std::cout << std::hex << std::setw(2) << std::setfill('0') << (int)b;
    std::cout << "\n";

    double proof = proof_of_exponomial(25, 5, 20, 3);
    std::cout << "Proof of Exponomial: " << proof << "\n";
}
