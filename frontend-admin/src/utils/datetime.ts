function pad(n: number): string {
  return n < 10 ? `0${n}` : `${n}`;
}

export function formatDateTime(value: unknown, fallback = '-'): string {
  if (value === null || value === undefined || value === '') return fallback;

  let date: Date;
  if (value instanceof Date) {
    date = value;
  } else if (typeof value === 'number') {
    date = new Date(value);
  } else if (Array.isArray(value) && value.length >= 3) {
    // Java LocalDateTime 数组: [yyyy, MM, dd, HH, mm, ss, nano]
    const [y, m, d, h = 0, mi = 0, s = 0] = value as number[];
    date = new Date(y!, m! - 1, d!, h, mi, s);
  } else if (typeof value === 'string') {
    // 已是 "YYYY-MM-DD HH:mm:ss" 格式则原样返回，避免被当作 UTC 解析后再次偏移
    const plain = /^\d{4}-\d{2}-\d{2}[ T]\d{2}:\d{2}:\d{2}(\.\d+)?$/;
    if (plain.test(value)) {
      return value.replace('T', ' ').split('.')[0]!;
    }
    date = new Date(value);
  } else {
    return fallback;
  }

  if (isNaN(date.getTime())) return fallback;

  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
}
