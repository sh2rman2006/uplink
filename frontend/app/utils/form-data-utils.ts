export function createChatFormData(data: {
  type: string;
  title?: string;
  description?: string;
  memberUserIds?: string[];
  coverFile?: File;
}): FormData {
  const fd = new FormData();

  fd.append("type", data.type);

  if (data.title != null) fd.append("title", data.title);
  if (data.description != null) fd.append("description", data.description);

  if (data.memberUserIds?.length) {
    for (const userId of data.memberUserIds) {
      fd.append("memberUserIds", userId);
    }
  }

  if (data.coverFile) {
    fd.append("coverFile", data.coverFile);
  }

  return fd;
}

export function createFilesMessageFormData(data: {
  files: File[];
  text?: string;
  replyToMessageId?: string;
}): FormData {
  const fd = new FormData();

  for (const f of data.files) fd.append("files", f);
  if (data.text != null) fd.append("text", data.text);
  if (data.replyToMessageId != null)
    fd.append("replyToMessageId", data.replyToMessageId);

  return fd;
}

export function createMediaMessageFormData(data: {
  files: File[];
  text?: string;
  replyToMessageId?: string;
}): FormData {
  // на бэке CreateMediaMessageRequest обычно тоже MultipartFile[] files
  const fd = new FormData();

  for (const f of data.files) fd.append("files", f);
  if (data.text != null) fd.append("text", data.text);
  if (data.replyToMessageId != null)
    fd.append("replyToMessageId", data.replyToMessageId);

  return fd;
}

export function createVoiceMessageFormData(data: {
  file: File;
  replyToMessageId?: string;
}): FormData {
  const fd = new FormData();

  // на бэке у тебя CreateVoiceMessageRequest, чаще всего поле называется "file"
  fd.append("file", data.file);
  if (data.replyToMessageId != null)
    fd.append("replyToMessageId", data.replyToMessageId);

  return fd;
}

export function editChatFormData(data: {
  title?: string;
  description?: string;
  coverFile?: File | null;
  allowSendMedia?: boolean;
  allowAddUsers?: boolean;
  allowPinMessages?: boolean;
  allowChangeInfo?: boolean;
}): FormData {
  const fd = new FormData();

  if (data.title !== undefined) fd.append("title", data.title ?? "");
  if (data.description !== undefined)
    fd.append("description", data.description ?? "");

  if (data.coverFile !== undefined) {
    if (data.coverFile) fd.append("coverFile", data.coverFile);
    else fd.append("coverFile", "");
  }

  if (data.allowSendMedia !== undefined)
    fd.append("allowSendMedia", String(data.allowSendMedia));
  if (data.allowAddUsers !== undefined)
    fd.append("allowAddUsers", String(data.allowAddUsers));
  if (data.allowPinMessages !== undefined)
    fd.append("allowPinMessages", String(data.allowPinMessages));
  if (data.allowChangeInfo !== undefined)
    fd.append("allowChangeInfo", String(data.allowChangeInfo));

  return fd;
}
