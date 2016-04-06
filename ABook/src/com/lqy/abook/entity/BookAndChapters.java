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

	public BookAndChapters(String url, List<ChapterEntity> chapters) {
		if (Util.isEmpty(url) || chapters == null || chapters.size() == 0) {
			result = SearchResult.Failed;
		} else {
			result = SearchResult.InputName;
			book = new BookEntity();
			book.setDirectoryUrl(url);
			book.setSite(Site.Other);
			this.chapters = chapters;
		}
	}

	public BookAndChapters(BookEntity book, List<ChapterEntity> chapters) {
		if (book != null) {
			this.book = book;
			if (chapters == null || chapters.size() == 0) {
				result = SearchResult.Search;
			} else {
				this.chapters = chapters;
				result = SearchResult.Success;
			}
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
