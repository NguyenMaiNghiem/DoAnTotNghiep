package com.example.eatitv2client.Callback;

import com.example.eatitv2client.Model.CommentModel;

import java.util.List;

public interface ICommentCallBackListener {
    void onCommentLoadSuccess(List<CommentModel> commentModels);
    void onCommentLoadFailed(String message);
}
