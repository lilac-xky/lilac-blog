import request from "@/request";

/** 获取文章列表(前台) POST /api/article/list/page/vo */
export async function listArticleVoByPage(
  body: API.ArticleQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultPageArticleVO>("/api/article/list/page/vo", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 获取文章详情 GET /api/article/get */
export async function getArticle(
  params: API.getArticleParams,
  options?: { [key: string]: any }
) {
  return request<API.ResultArticleVO>("/api/article/get", {
    method: "GET",
    params: { ...params },
    ...(options || {}),
  });
}
