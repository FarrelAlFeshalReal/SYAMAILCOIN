import struct
from decimal import Decimal, getcontext
from base58 import b58encode
import math
import time

getcontext().prec = 28

def sai288_hash(data_bytes):
    h = sum(data_bytes[i] << (8*(i%36)) for i in range(len(data_bytes))) % (2**288)
    return h.to_bytes(36, byteorder='big')

def exponomial_constant(i, gamma, R, tau, S_list, phi):
    exp_factor = gamma ** (i / R)
    ssum = sum(S_list[j] * (phi ** j) for j in range(0, min(len(S_list), i+1)))
    return exp_factor * tau * ssum

def proof_of_exponomial(n, r, delta_n, delta_r):
    try:
        term1 = math.factorial(n) / (math.factorial(r) * math.factorial(n-r))
        term2 = math.factorial(delta_n) / (math.factorial(delta_r) * math.factorial(delta_n-delta_r))
        return abs(term1 - term2)
    except:
        return 0

def generate_sai288_hash(block_index, reward, nonce):
    raw_bytes = struct.pack('>QdQ', block_index, float(reward), nonce)
    return b58encode(sai288_hash(raw_bytes)).decode()

if __name__ == '__main__':
    S_list = [1.0 for _ in range(10)]
    block_index = 0
    reward = Decimal('0.123456')
    nonce = int(time.time() * 1000)
    hash_value = generate_sai288_hash(block_index, reward, nonce)
    print(hash_value)
