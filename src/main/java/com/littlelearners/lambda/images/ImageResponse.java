package com.littlelearners.lambda.images;

import java.util.List;

public class ImageResponse {
	List<String> imageUrls;

	public ImageResponse(List<String> imageUrls) {
		this.imageUrls = imageUrls;
	}
	public List<String> getImageUrls() {
		return imageUrls;
	}

	public void setImageUrls(List<String> imageUrls) {
		this.imageUrls = imageUrls;
	}
	
}
