// useDateFormat：前台日期格式化通用 hook
// 抽出来是因为后端时间字段均为 ISO 字符串，多处需要统一展示样式
import dayjs from 'dayjs';

export function useDateFormat() {
  function formatDate(d?: string | number | null, pattern = 'YYYY-MM-DD HH:mm') {
    return d ? dayjs(d).format(pattern) : '';
  }

  function formatDateOnly(d?: string | number | null) {
    return formatDate(d, 'YYYY-MM-DD');
  }

  return { formatDate, formatDateOnly };
}
