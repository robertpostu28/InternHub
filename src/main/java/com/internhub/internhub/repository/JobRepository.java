package com.internhub.internhub.repository;

import com.internhub.internhub.domain.Job;
import com.internhub.internhub.domain.enums.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends  JpaRepository<Job,Long> {
    /*
        Pageable is an interface provided by Spring Data that allows you to specify pagination information such as page
        number, page size, and sorting criteria when querying the database. It is used in repository methods to enable
        pagination of results, which is especially useful when dealing with large datasets. By using Pageable, you can
        efficiently retrieve a subset of data based on the specified pagination parameters.

        Example usage:
        Pageable pageable = PageRequest.of(0, 10); // Get the first page with 10 items per page

        When you use pagination with Pageable, Spring Data will generate a SQL query that includes a LIMIT and OFFSET clause
        to retrieve only the specified subset of results.

        For example, if you request page 0 with a page size of 10, the generated SQL query will include "LIMIT 10 OFFSET 0", which
        means it will retrieve the first 10 records. If you request page 1 with a page size of 10, the generated SQL query will
        include "LIMIT 10 OFFSET 10", which means it will retrieve the next 10 records starting from the 11th record.

        This allows you to efficiently navigate through large datasets without loading all records into memory at once, improving
        performance and user experience.
    */
    Page<Job> findByStatus(JobStatus status, Pageable pageable);
}
