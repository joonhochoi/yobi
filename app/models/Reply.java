package models;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.data.validation.Constraints;
import play.db.ebean.Model;

@Entity
public class Reply extends Model {
    private static final long serialVersionUID = 1L;

    public Reply() {
        date = new Date();
    }

    @Id
    public int replyNum;
    public Long articleNum;
    @Constraints.Required
    public String contents;
    public Long writerId;
    @Constraints.Required
    public Date date;
    public String filePath;
    
    public static Finder<Long, Reply> find = new Finder<Long, Reply>(Long.class, Reply.class);

    public static List<Reply> findArticlesReply(Long articleNum) {
        return find.where().eq("articleNum", articleNum).findList();
    }

    public static long write(Reply reply) {
        Article.replyAdd(reply.articleNum);
        reply.save();
        return reply.replyNum;
    }

    public static void deleteByArticleNum(Long articleNum) {
        List<Reply> targets = Reply.find.where().eq("articleNum", "" + articleNum).findList();

        // 루프 돌면서 삭제
        Iterator<Reply> target = targets.iterator();
        while (target.hasNext()) {
            Reply reply = target.next();
            reply.delete();
        }
    }

    public String calcPassTime() {
        // TODO 경계값 검사하면 망할함수. 나중에 라이브러리 쓸예정
        Calendar today = Calendar.getInstance();

        long dTimeMili = today.getTime().getTime() - this.date.getTime();

        Calendar dTime = Calendar.getInstance();
        dTime.setTimeInMillis(dTimeMili);

        if (dTimeMili < 60 * 1000) {
            return "방금 전";
        } else if (dTimeMili < 60 * 1000 * 60) {
            return dTime.get(Calendar.MINUTE) + "분 전";
        } else if (dTimeMili < 60 * 1000 * 60 * 24) {
            return dTime.get(Calendar.HOUR) + "시간 전";
        } else if (dTimeMili < 60 * 1000 * 60 * 24 * 30) {
            return dTime.get(Calendar.DATE) + "일 전";
        } else if (dTimeMili < 60 * 1000 * 60 * 24 * 30 * 12) {
            return dTime.get(Calendar.MONDAY) + "달 전";
        } else {
            return dTime.get(Calendar.YEAR) + "년 전";
        }
    }

    public String writer() {
        return User.findById(writerId).name;
    }
}