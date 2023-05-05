package com.easybbs.controller;

import com.easybbs.controller.base.ABaseController;
import com.easybbs.entity.annotation.GlobalIntercepter;
import com.easybbs.entity.annotation.VerifyParams;
import com.easybbs.entity.dto.SessionWebUserDto;
import com.easybbs.entity.enums.ArticleStatusEnum;
import com.easybbs.entity.enums.OperRecordOpTypeEnum;
import com.easybbs.entity.enums.PageSize;
import com.easybbs.entity.po.ForumComment;
import com.easybbs.entity.po.LikeRecord;
import com.easybbs.entity.query.ForumCommentQuery;
import com.easybbs.entity.vo.ResponseVO;
import com.easybbs.exception.BusinessException;
import com.easybbs.service.ForumCommentService;
import com.easybbs.service.LikeRecordService;
import com.easybbs.utils.SysCacheUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 * @ClassName ForumCommentController
 * @Description TODO
 * @Date 2023/5/5 8:58
 * @Created by pepedd
 */
@RestController
@RequestMapping("/comment")
public class ForumCommentController extends ABaseController {
  @Resource
  private ForumCommentService forumCommentService;
  @Resource
  private LikeRecordService likeRecordService;

  /**
   * 加载评论
   *
   * @param session   会话
   * @param articleId 文章id
   * @param pageNo    页码
   * @param orderType 排序类型
   * @return
   */
  @GetMapping("/loadComment")
  @GlobalIntercepter(checkParams = true)
  public ResponseVO loadComment(HttpSession session, @VerifyParams(required = true) String articleId, Integer pageNo, Integer orderType) {
    final String ORDER_TYPE0 = "good_count desc,comment_id asc";
    final String ORDER_TYPE1 = "comment_id desc";
    if (!SysCacheUtils.getSysSetting().getCommentSetting().getCommentEnable()) {
      throw new BusinessException("评论功能已关闭");
    }
    ForumCommentQuery commentQuery = new ForumCommentQuery();
    commentQuery.setArticleId(articleId);
    String orderBy = orderType == null || orderType == 0 ? ORDER_TYPE0 : ORDER_TYPE1;
    commentQuery.setOrderBy("top_type desc," + orderBy);
    SessionWebUserDto userDto = getUserInfoFromSession(session);
    if (userDto != null) {
      commentQuery.setQueryLikeType(true);
      commentQuery.setCurrentUserId(userDto.getUserId());
    } else {
      commentQuery.setStatus(ArticleStatusEnum.AUDIT.getStatus());
    }
    commentQuery.setPageNo(pageNo);
    commentQuery.setPageSize(PageSize.SIZE50.getSize());
    commentQuery.setpCommentId(0);
    commentQuery.setLoadChildren(true);
    return getSuccessResponseVO(forumCommentService.findListByPage(commentQuery));
  }

  /**
   * 评论点赞
   *
   * @param session   会话
   * @param commentId 评论id
   * @return
   */
  @RequestMapping("/doLike")
  @GlobalIntercepter(checkLogin = true, checkParams = true)
  public ResponseVO doLike(HttpSession session, @VerifyParams(required = true) Integer commentId) {
    SessionWebUserDto userDto = getUserInfoFromSession(session);
    String objId = String.valueOf(commentId);
    likeRecordService.doLike(objId, userDto.getUserId(), userDto.getNickName(), OperRecordOpTypeEnum.COMMENT_LIKE);
    LikeRecord likeRecord = likeRecordService.getLikeRecordByObjectIdAndUserIdAndOpType(objId, userDto.getUserId(), OperRecordOpTypeEnum.COMMENT_LIKE.getType());
    ForumComment forumComment = forumCommentService.getForumCommentByCommentId(commentId);
    forumComment.setLikeType(likeRecord == null ? 0 : 1);

    return getSuccessResponseVO(null);
  }

  /**
   * 评论
   *
   * @param session   会话
   * @param commentId 评论id
   * @param topType   置顶类型
   * @return
   */
  @RequestMapping("/changeTopType")
  @GlobalIntercepter(checkLogin = true, checkParams = true)
  public ResponseVO changeTopType(HttpSession session, @VerifyParams(required = true) Integer commentId, @VerifyParams(required = true) Integer topType) {
    forumCommentService.changeTopType(getUserInfoFromSession(session).getUserId(), commentId, topType);
    return getSuccessResponseVO(null);
  }
}
