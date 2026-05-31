
export interface FileBlock {
  index: number;
  start: number;
  end: number;
  size: number;
  blob: Blob;
}

export function splitFileIntoBlocks(file: File, blockSizeBytes: number): FileBlock[] {
  if (blockSizeBytes <= 0) {
    throw new Error('Blocksize must be greater than zero');
  }

  const blocks: FileBlock[] = [];

  for (let start = 0 , index = 0; start < file.size; start +=blockSizeBytes, index++) {
    const end = Math.min(start + blockSizeBytes, file.size);

    blocks.push({
      index,
      start,
      end,
      size: end - start,
      blob: file.slice(start, end),
    });
  }
  return blocks;
}
