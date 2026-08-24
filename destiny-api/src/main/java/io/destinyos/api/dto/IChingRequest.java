package io.destinyos.api.dto;

/**
 * Input for the I Ching / Mai Hoa engine (Phase — R12, resolved 2026-08-24).
 *
 * <p>Present in a {@link ScenarioRunRequest} only when the caller wants this
 * engine to participate. Its absence is not an error.
 *
 * <p>Unlike Bát Tự or Phong Thủy, this engine needs no birth data at all —
 * a hexagram casting is about a question asked now, not a person born then
 * (the same reasoning {@code TarotRequest} already follows).
 *
 * @param method       {@code "THREE_COINS"}, {@code "YARROW"},
 *                     {@code "MAI_HOA_NUMBER"} or {@code "MAI_HOA_TIME"}
 *                     (case-insensitive). Required — this engine never
 *                     defaults a casting method, since the four are a real
 *                     methodological choice (different probability
 *                     distributions, or deterministic arithmetic instead of
 *                     randomness), not an implementation detail
 * @param seed         for {@code THREE_COINS}/{@code YARROW}: caller-supplied
 *                     seed for reproducibility, or {@code null} to let the
 *                     engine generate one via CSPRNG and report it back
 * @param upperNumber  for {@code MAI_HOA_NUMBER}: the number forming the
 *                     upper trigram. Both numbers are required together — a
 *                     single multi-digit number is not accepted in this
 *                     version (see {@code IChingEngine}'s Javadoc for why)
 * @param lowerNumber  for {@code MAI_HOA_NUMBER}: the number forming the
 *                     lower trigram
 */
public record IChingRequest(
        String method,
        Long seed,
        Integer upperNumber,
        Integer lowerNumber
) {
}
