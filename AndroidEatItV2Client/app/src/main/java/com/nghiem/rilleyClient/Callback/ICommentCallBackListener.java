package com.nghiem.rilleyClient.Callback;

import com.nghiem.rilleyClient.Model.CommentModel;

import java.util.List;

public interface ICommentCallBackListener {
    void onCommentLoadSuccess(List<CommentModel> commentModels);
    void onCommentLoadFailed(String message);
}
