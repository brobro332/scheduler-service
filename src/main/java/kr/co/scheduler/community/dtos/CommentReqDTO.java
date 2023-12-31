package kr.co.scheduler.community.dtos;

import kr.co.scheduler.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class CommentReqDTO {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class CREATE {

    private String comment;

    public CREATE(){
    }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class UPDATE {

        private String updateComment;

        // 기본 생성자 추가
        public UPDATE() {
        }
    }
}


