// @ts-ignore
/* eslint-disable */
import request from "@/request";

/** 文件上传 POST /api/file/upload */
export async function uploadFile(
  body: {},
  file?: File,
  options?: { [key: string]: any }
) {
  const formData = new FormData();

  if (file) {
    formData.append("file", file);
  }

  Object.keys(body).forEach((ele) => {
    const item = (body as any)[ele];

    if (item !== undefined && item !== null) {
      if (typeof item === "object" && !(item instanceof File)) {
        if (item instanceof Array) {
          item.forEach((f) => formData.append(ele, f || ""));
        } else {
          formData.append(
            ele,
            new Blob([JSON.stringify(item)], { type: "application/json" })
          );
        }
      } else {
        formData.append(ele, item);
      }
    }
  });

  return request<API.ResultUploadPictureResult>("/api/file/upload", {
    method: "POST",
    data: formData,
    ...(options || {}),
  });
}

/** 通过外链 URL 上传 POST /api/file/uploadByUrl */
export async function uploadByUrl(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.uploadByUrlParams,
  options?: { [key: string]: any }
) {
  return request<API.ResultUploadPictureResult>("/api/file/uploadByUrl", {
    method: "POST",
    params: {
      ...params,
    },
    ...(options || {}),
  });
}
