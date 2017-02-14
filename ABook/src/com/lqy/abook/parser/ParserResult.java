package com.lqy.abook.parser;

import java.util.List;

import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.entity.ChapterEntity;
import com.lqy.abook.entity.SiteEnum;
import com.lqy.abook.tool.Util;

public class ParserResult {

	public static enum Result {
		Success, Failed, Search, InputName
	}

	private Result result;
	private BookEntity book;
	private List<ChapterEntity> chapters;

	public ParserResult(String name, String author) {
		result = Result.Search;
		book = new BookEntity();
		book.setName(name);
		book.setAuthor(author);
	}

	public ParserResult(String url, List<ChapterEntity> chapters) {
		if (Util.isEmpty(url) || chapters == null || chapters.size() == 0) {
			result = Result.Failed;
		} else {
			result = Result.InputName;
			book = new BookEntity();
			book.setDirectoryUrl(url);
			book.setSite(SiteEnum.Other);
			this.chapters = chapters;
		}
	}

	public ParserResult(BookEntity book, List<ChapterEntity> chapters) {
		if (book != null) {
			this.book = book;
			if (chapters == null || chapters.size() == 0) {
				result = Result.Search;
			} else {
				this.chapters = chapters;
				result = Result.Success;
			}
		}
	}

	public Result getResult() {
		return result;
	}

	public void setResult(Result result) {
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
