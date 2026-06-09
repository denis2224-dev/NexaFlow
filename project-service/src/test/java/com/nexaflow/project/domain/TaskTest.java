package com.nexaflow.project.domain;

import static com.nexaflow.project.domain.CommentTestSamples.*;
import static com.nexaflow.project.domain.ProjectTestSamples.*;
import static com.nexaflow.project.domain.TaskTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.nexaflow.project.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TaskTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Task.class);
        Task task1 = getTaskSample1();
        Task task2 = new Task();
        assertThat(task1).isNotEqualTo(task2);

        task2.setId(task1.getId());
        assertThat(task1).isEqualTo(task2);

        task2 = getTaskSample2();
        assertThat(task1).isNotEqualTo(task2);
    }

    @Test
    void commentsTest() {
        Task task = getTaskRandomSampleGenerator();
        Comment commentBack = getCommentRandomSampleGenerator();

        task.addComments(commentBack);
        assertThat(task.getCommentses()).containsOnly(commentBack);
        assertThat(commentBack.getTask()).isEqualTo(task);

        task.removeComments(commentBack);
        assertThat(task.getCommentses()).doesNotContain(commentBack);
        assertThat(commentBack.getTask()).isNull();

        task.commentses(new HashSet<>(Set.of(commentBack)));
        assertThat(task.getCommentses()).containsOnly(commentBack);
        assertThat(commentBack.getTask()).isEqualTo(task);

        task.setCommentses(new HashSet<>());
        assertThat(task.getCommentses()).doesNotContain(commentBack);
        assertThat(commentBack.getTask()).isNull();
    }

    @Test
    void projectTest() {
        Task task = getTaskRandomSampleGenerator();
        Project projectBack = getProjectRandomSampleGenerator();

        task.setProject(projectBack);
        assertThat(task.getProject()).isEqualTo(projectBack);

        task.project(null);
        assertThat(task.getProject()).isNull();
    }
}
