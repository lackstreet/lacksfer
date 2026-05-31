import { splitFileIntoBlocks } from './file-blocks';

describe('splitFileIntoBlocks', () => {
  it('should split a file into fixed-size blocks', () => {
    const file = new File(['abcdefghij'], 'test.txt');

    const blocks = splitFileIntoBlocks(file, 4);

    expect(blocks.length).toBe(3);

    expect(blocks[0].index).toBe(0);
    expect(blocks[0].start).toBe(0);
    expect(blocks[0].end).toBe(4);
    expect(blocks[0].size).toBe(4);

    expect(blocks[1].index).toBe(1);
    expect(blocks[1].start).toBe(4);
    expect(blocks[1].end).toBe(8);
    expect(blocks[1].size).toBe(4);

    expect(blocks[2].index).toBe(2);
    expect(blocks[2].start).toBe(8);
    expect(blocks[2].end).toBe(10);
    expect(blocks[2].size).toBe(2);
  });

  it('should reject invalid block size', () => {
    const file = new File(['abc'], 'test.txt');

    expect(() => splitFileIntoBlocks(file, 0)).toThrow();
  });
});
