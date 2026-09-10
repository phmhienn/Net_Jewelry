import { api } from "./api";
import { isDemo } from "../data/config";
import type { Address } from "../types";
import { readLocal, writeLocal } from "../utils/storage";
export const accountService = {
  async getAddress(email: string): Promise<Address | null> {
    return isDemo
      ? readLocal(`net-address:${email.toLowerCase()}`, null)
      : (await api.get<Address | null>("/account/address")).data;
  },
  async saveAddress(email: string, address: Address): Promise<Address> {
    if (!isDemo)
      return (await api.put<Address>("/account/address", address)).data;
    writeLocal(`net-address:${email.toLowerCase()}`, address);
    return address;
  },
};
