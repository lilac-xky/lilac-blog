import request from "@/request";

/** 获取标签列表(用户) POST /api/tag/list/page/vo */
export async function listTagByPageVo(
  body: API.TagQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultPageTagVO>("/api/tag/list/page/vo", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}
