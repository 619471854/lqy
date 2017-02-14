package com.lqy.abook.entity;

import java.util.List;

import com.lqy.abook.tool.Util;

public class BookAndChapters {

	public static enum SearchResult {
		Success, Failed, Search, InputName
	}

	private SearchResult result;
	private BookEntity book;
	private List<ChapterEntity> chapters;

	public BookAndChapters(String name, String author) {
		result = SearchResult.Search;
		book = new BookEntity();
		book.setName(name);
		book.setAuthor(author);
	}

	public BookAndChapters(BookEntity book, List<ChapterEntity> chapters) {
		if (book != null) {
			this.book = book;
			if (book.getSite() == SiteEnum.Single) {
				result = SearchResult.InputName;
			} else if (chapters == null || chapters.size() == 0) {
				if (Util.isEmpty(book.getName()))
					result = SearchResult.Failed;
				else
					result = SearchResult.Search;
			} else {
				this.chapters = chapters;
				if (Util.isEmpty(book.getName()))
					result = SearchResult.InputName;
				else
					result = SearchResult.Success;
			}
		} else {
			result = SearchResult.Failed;
		}
	}

	public SearchResult getResult() {
		return result;
	}

	public void setResult(SearchResult result) {
		this.result = result;
	}

	public BookEntity getBook() {
		return book;
	}

	public void setBook(BookEntity book) {
		this.book = book;
	}

	public List<ChapterEntity> getChapters() {
		return chapters;
	}

	public void setChapters(List<ChapterEntity> chapters) {
		this.chapters = chapters;
	}

}
