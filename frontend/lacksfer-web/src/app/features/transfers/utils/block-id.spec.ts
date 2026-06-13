import { createBlockId } from './block-id';

describe('createBlockId', () => {
  it('should create stable base64 block ids', () => {
    expect(createBlockId(0)).toBe('MDAwMDAw');
    expect(createBlockId(1)).toBe('MDAwMDAx');
    expect(createBlockId(10)).toBe('MDAwMDEw');
    expect(createBlockId(42)).toBe('MDAwMDQy');
  });

  it('should reject negative indexes', () => {
    expect(() => createBlockId(-1)).toThrow();
  });

  it('should reject decimal indexes', () => {
    expect(() => createBlockId(1.5)).toThrow();
  });
});
