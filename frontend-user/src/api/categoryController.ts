import request from "@/request";

/** 获取分类列表(用户) POST /api/category/list/page/vo */
export async function listCategoryByPageVo(
  body: API.CategoryQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultPageCategoryVO>("/api/category/list/page/vo", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}
