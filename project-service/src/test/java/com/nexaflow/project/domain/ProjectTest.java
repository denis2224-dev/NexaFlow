package com.nexaflow.project.domain;

import static com.nexaflow.project.domain.ProjectTestSamples.*;
import static com.nexaflow.project.domain.TaskTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.nexaflow.project.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ProjectTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Project.class);
        Project project1 = getProjectSample1();
        Project project2 = new Project();
        assertThat(project1).isNotEqualTo(project2);

        project2.setId(project1.getId());
        assertThat(project1).isEqualTo(project2);

        project2 = getProjectSample2();
        assertThat(project1).isNotEqualTo(project2);
    }

    @Test
    void tasksTest() {
        Project project = getProjectRandomSampleGenerator();
        Task taskBack = getTaskRandomSampleGenerator();

        project.addTasks(taskBack);
        assertThat(project.getTaskses()).containsOnly(taskBack);
        assertThat(taskBack.getProject()).isEqualTo(project);

        project.removeTasks(taskBack);
        assertThat(project.getTaskses()).doesNotContain(taskBack);
        assertThat(taskBack.getProject()).isNull();

        project.taskses(new HashSet<>(Set.of(taskBack)));
        assertThat(project.getTaskses()).containsOnly(taskBack);
        assertThat(taskBack.getProject()).isEqualTo(project);

        project.setTaskses(new HashSet<>());
        assertThat(project.getTaskses()).doesNotContain(taskBack);
        assertThat(taskBack.getProject()).isNull();
    }
}
