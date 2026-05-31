
export function createBlockId(index: number): string {
  if (!Number.isInteger(index) || index < 0) {
    throw new Error('Block index must be a non-negative integer');
  }

  const paddedIndex = index.toString().padStart(6, '0');

  return btoa(paddedIndex);
}
