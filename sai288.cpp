#include <iostream>
#include <vector>
#include <cmath>
#include <cstdint>
#include <iomanip>

uint64_t factorial(int n){
    uint64_t result = 1;
    for(int i=2;i<=n;++i) result *= i;
    return result;
}

double exponomial_constant(int i, double gamma, double R, double tau, std::vector<double> S_list, double phi){
    double exp_factor = pow(gamma, i / R);
    double ssum = 0.0;
    for(int j=0;j<=std::min(i,(int)S_list.size()-1);++j)
        ssum += S_list[j]*pow(phi,j);
    return exp_factor*tau*ssum;
}

double proof_of_exponomial(int n, int r, int delta_n, int delta_r){
    try{
        double term1 = factorial(n)/(factorial(r)*factorial(n-r));
        double term2 = factorial(delta_n)/(factorial(delta_r)*factorial(delta_n-delta_r));
        return fabs(term1 - term2);
    }catch(...){
        return 0.0;
    }
}

std::vector<uint8_t> sai288_hash(const std::vector<uint8_t>& data){
    // 288-bit hash → 36 byte
    __uint128_t h1 = 0, h2 = 0; // gunakan 128-bit untuk split sum
    for(size_t i=0;i<data.size();++i){
        if(i<16) h1 += ((__uint128_t)data[i]) << (8*(i%16));
        else h2 += ((__uint128_t)data[i]) << (8*((i-16)%16));
    }
    std::vector<uint8_t> out(36,0);
    for(int i=35;i>=18;i--){ out[i] = (h2 >> (8*(i-18))) & 0xFF; }
    for(int i=17;i>=0;i--){ out[i] = (h1 >> (8*i)) & 0xFF; }
    return out;
}

int main(){
    std::vector<double> S_list(10,1.0);
    int block_index = 0;
    double reward = 0.123456;
    std::vector<uint8_t> raw_bytes(16);
    for(int i=0;i<raw_bytes.size();i++) raw_bytes[i] = (i+block_index+1)%256;

    auto hash = sai288_hash(raw_bytes);
    std::cout<<"SAI288 Hash: ";
    for(auto b: hash) std::cout<<std::hex<<std::setw(2)<<std::setfill('0')<<(int)b;
    std::cout<<"\n";
}
