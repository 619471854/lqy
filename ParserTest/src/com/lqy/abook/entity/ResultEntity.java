package com.lqy.abook.entity;

public class ResultEntity extends BaseEntity {
	private boolean error;
	private String msg;

	public static ResultEntity error(String msg) {
		ResultEntity e = new ResultEntity();
		e.error = true;
		e.msg = msg;
		return e;
	}

	public static ResultEntity success(String msg) {
		ResultEntity e = new ResultEntity();
		e.msg = msg;
		return e;
	}

	public String toString() {
		return error ? "error:" + msg : msg;
	}

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

}
