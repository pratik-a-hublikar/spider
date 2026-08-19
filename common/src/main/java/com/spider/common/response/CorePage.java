package com.spider.common.response;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class CorePage<D> implements Serializable {

    @Serial
    private static final long serialVersionUID = 4062527758271926063L;

    private int currentPage;

    private int numberOfElement;

    private int size;

    private int totalPages;

    private long totalElements;

    private Iterable<D> content;


    private static <C> CorePage<C> of(Iterable<C> content, int currentPage, int numberOfElement, int size, int totalPages,
                                      long totalElements) {
        CorePage<C> corePage = new CorePage<>();
        corePage.setContent(content);
        corePage.setCurrentPage(currentPage);
        corePage.setNumberOfElement(numberOfElement);
        corePage.setSize(size);
        corePage.setTotalPages(totalPages);
        corePage.setTotalElements(totalElements);
        return corePage;
    }
    public static <C> CorePage<C> ofNew(Iterable<C> content, int currentPage, int numberOfElement, int size, int totalPages,
                                     long totalElements) {
        return of(content, currentPage, numberOfElement, size, totalPages, totalElements);
    }
    public static <C> CorePage<C> of(Iterable<C> content, Page page) {
        return of(content, page.getNumber(), page.getNumberOfElements(), page.getSize(), page.getTotalPages(), page.getTotalElements());
    }

    public static <C> CorePage<C> of(Iterable<C> content, CorePage page) {
        return of(content, page.getCurrentPage(), page.getNumberOfElement(), page.getSize(), page.getTotalPages(), page.getTotalElements());
    }

    public static <C> CorePage<C> of(Page page) {
        return of(page.getContent(), page.getNumber(), page.getNumberOfElements(), page.getSize(), page.getTotalPages(), page.getTotalElements());
    }

}
