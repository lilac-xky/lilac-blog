// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** 添加文章 POST /api/article/add */
export async function addArticle(
  body: API.ArticleAddRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultLong>("/api/article/add", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 删除文章 POST /api/article/delete */
export async function deleteArticle(
  body: API.DeleteRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>("/api/article/delete", {
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
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getArticleParams,
  options?: { [key: string]: any }
) {
  return request<API.ResultArticleVO>("/api/article/get", {
    method: "GET",
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** 获取文章列表(管理员) POST /api/article/list/page */
export async function listArticleByPage(
  body: API.ArticleQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultPageArticleVO>("/api/article/list/page", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

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

/** 文章审核（通过 / 驳回） POST /api/article/review */
export async function reviewArticle(
  body: API.ArticleReviewRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>("/api/article/review", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 更新文章 POST /api/article/update */
export async function updateArticle(
  body: API.ArticleUpdateRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>("/api/article/update", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 普通用户删除自己的文章 POST /api/article/user/delete */
export async function deleteMyArticle(
  body: API.DeleteRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>("/api/article/user/delete", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 分页查询我的文章 POST /api/article/user/my/page */
export async function listMyArticles(
  body: API.ArticleQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultPageArticleVO>("/api/article/user/my/page", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 普通用户提交文章（自动进入待审核） POST /api/article/user/submit */
export async function submitMyArticle(
  body: API.ArticleAddRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultLong>("/api/article/user/submit", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** 普通用户更新自己的文章 POST /api/article/user/update */
export async function updateMyArticle(
  body: API.ArticleUpdateRequest,
  options?: { [key: string]: any }
) {
  return request<API.ResultBoolean>("/api/article/user/update", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}
