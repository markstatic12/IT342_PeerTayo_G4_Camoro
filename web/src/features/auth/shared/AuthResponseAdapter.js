export function adaptAuthPayload(payload) {
  return {
    user: payload?.user ?? null,
    token: payload?.token ?? null,
    refreshToken: payload?.refreshToken ?? null,
  };
}

export function adaptUserPayload(payload) {
  return {
    user: payload ?? null,
    token: null,
  };
}
