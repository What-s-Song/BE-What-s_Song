package dy.whatsong.domain.story.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import dy.whatsong.domain.member.dto.MemberDto;
import dy.whatsong.domain.story.dto.StoryVideo;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.time.LocalDateTime;

@RedisHash(timeToLive = 86400,value = "story")
@Getter
@Builder
public class Story implements Serializable {

    @Id
    private String id;
    private MemberDto.MemberStory memberStory;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime postTime;
    private String img_url;
    private String start;
    private String end;
    private StoryVideo storyVideo;
}
