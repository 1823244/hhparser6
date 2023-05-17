package com.ens.hhparser5.service;

import com.ens.hhparser5.model.*;
import com.ens.hhparser5.repository.ProjectRepo;
import com.ens.hhparser5.utility.ClockHolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class ProjectService {
    private final Logger logger = LoggerFactory.getLogger(ProjectService.class);

    @Autowired
    private ProjectRepo projectRepo;
    @Autowired
    private SearchHistoryService searchHistoryService;
    @Autowired
    private PublicationHistoryService publicationHistoryService;
    @Autowired
    private HhruService hhruService;
    @Autowired
    private VacancyService vacancyService;
    @Autowired
    private EmployerService employerService;
    @Autowired
    private SearchTextService searchTextService;
    @Autowired
    private TaskService taskService;

    /**
     * http GET handler: /project/searchall
     */
    public void searchAllProjects(User user) {

        Task task = taskService.createTask(null,
                "Process all",
                LocalDateTime.now(ClockHolder.getClock()),
                user);

        final java.sql.Date currentDate = java.sql.Date.valueOf(LocalDate.now());

        // get list of projects
        final List<Project> projects = projectRepo.findAll(user);
        //for (ProjectDto project: projects){
        //    processOneProject(project.getName(), currentDate);
        //}
        projects.stream().forEach(
                p -> {
                    try {
                        processOneProject(p, currentDate);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }
        );

        taskService.finishTask(task);
    }

    /**
     * ProjectController handler
     * http GET handler: /project/search/{projectName}
     */
    public void searchOneProject(Project project, User user) throws JsonProcessingException {

        final java.sql.Date currentDate = java.sql.Date.valueOf(LocalDate.now());

        // todo Need check if we already run a task which is processing all projects
        // ( from method searchAllProjects() )
        // if so, we must not start the processing of certain project
        Task task = taskService.createTask(project,
                "Process project: "+project.getName(),
                LocalDateTime.now(ClockHolder.getClock()),
                user);

        processOneProject(project, currentDate);

        taskService.finishTask(task);
    }

    /**
     * Запускает поиск вакансий по проекту
     * @param project
     * @param currentDate
     * @throws JsonProcessingException
     */
    private void processOneProject(final Project project, final java.sql.Date currentDate) throws JsonProcessingException {

        long projectId = project.getId();
        searchHistoryService.clearHistoryByProject(projectId);

        // get list of search texts within one project
        final List<SearchText> searchTextsList = searchTextService.findAllByProject(project);


        // get Map with Maps. Maps of the second levels contains Lists with vacancies
        // Map<String, Map>
        //      "all"               - Map<String, VacancySourceDto> - all vacancies by the project. the key is string "all".
        //                                  "321654987" -  VacancySourceDto  - hh id as the key
        //      "java+developer"    - Map<String, VacancySourceDto> - vacancies by the one search text. Search text uses as the key.
        //      another search texts as a key....
        //
        final Map<String, Map> result = hhruService.processSearchTextsListVersionSubproject(
                searchTextsList.stream().map(p->p.getText()).toList(), currentDate );

        Map<String, VacancySource> allVacanciesByProject = result.get("all");
        logger.info("total vacancies for processing: {}", allVacanciesByProject.size());

        // А это реальная обработка списка вакансий

        logger.info("total vacancies for processing: {}", allVacanciesByProject.size());

        // сначала в один поток сохраним работодателей
        employerService.parseAndSaveUniqueEmployers(allVacanciesByProject);

        int counter = 1;
        for (VacancySource vacDto : allVacanciesByProject.values()) {
            logger.info("processing vacancy #{} id {}",counter++,vacDto.getHhid());
            vacancyService.processOneVacancy(vacDto, projectId, currentDate);
        }

        //2022-08-20 replace row-by-row to batch processing
        //publicationHistoryService.vacancyClosingProcess(project_id, currentDate);
        // 2023-01-08 disable for debug postgres
        publicationHistoryService.vacancyClosingProcessWithSQLQuery(projectId, currentDate);

        // process distinct search texts one by one
        Set<String> keys = result.keySet();
        for (String key:keys) {
            if (key.equals("all")) {
                continue;//already processed. skip
            }
            SearchText stextDto = searchTextsList.stream().filter(p->p.getText().equals(key)).findAny().orElseThrow();
            Map<String, VacancySource> oneSearchTextMap = result.get(key);
            for (VacancySource vacDto2 : oneSearchTextMap.values()) {
                logger.info("processing vacancy #{} id {}",counter++,vacDto2.getHhid());
                vacancyService.processOneVacancyBySearchText(vacDto2, projectId, stextDto.getId(), currentDate);
            }
            publicationHistoryService.vacancyClosingProcessWithSQLQueryBySearchText(projectId, stextDto.getId(), currentDate);
        }
    }

    /**
     * Пример работы с Optional
     * @param projectId
     * @return
     */
    public Project findById(long projectId){
        Optional<Project> project = Optional.ofNullable(projectRepo.findById(projectId));
        if (!project.isEmpty()) {
            return project.get();
        } else {
            throw new RuntimeException("Cannot find project with ID <"+String.valueOf(projectId)+">");
        }
    }

    public List<Project> findAll(User user) {
        return projectRepo.findAll(user);
    }

    public void save(Project project) {
        projectRepo.save(project);
    }

    public void update(Project project) {
        projectRepo.update(project);
    }

    public void delete(Project project) {
        projectRepo.delete(project);
    }
}
