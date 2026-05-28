// CRUD 通用类型定义：搜索表单字段、弹窗表单字段、表格列与列渲染器
import type { Rule } from 'ant-design-vue/es/form';
import type { TableColumnType } from 'ant-design-vue';

export type SortOrder = 'descend' | 'ascend';

export interface SelectOption {
  label: string;
  value: string | number;
}

export interface SearchField {
  name: string;
  label: string;
  type: 'input' | 'select';
  placeholder?: string;
  allowClear?: boolean;
  width?: number | string;
  options?: SelectOption[];
}

export interface FormField {
  name: string;
  label?: string;
  type: 'input' | 'textarea' | 'select' | 'switch' | 'slot';
  placeholder?: string;
  allowClear?: boolean;
  options?: SelectOption[];
  rows?: number;
  rules?: Rule[];
  span?: number;
}

// 内置列渲染器：声明 renderer 后由 TableCRUD 自动渲染对应控件，避免每页都写自定义模板
export type ColumnRenderer =
  | { type: 'datetime' }
  | { type: 'statusTag'; map: Record<string | number, { label: string; color?: string }> }
  | { type: 'avatar'; size?: number; fallbackIcon?: boolean }
  | {
    type: 'switch';
    getChecked: (record: any) => boolean;
    onChange: (record: any, val: boolean) => void;
    loadingKey?: string;
    disabled?: (record: any) => boolean;
    checkedText?: string;
    uncheckedText?: string;
  };

// 在 antd 列上扩展 renderer，列定义即可声明渲染策略
export type CrudColumn = TableColumnType & {
  renderer?: ColumnRenderer;
};
