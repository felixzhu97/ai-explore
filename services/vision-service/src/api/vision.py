from fastapi import APIRouter, UploadFile, File, HTTPException, Depends, Body, Request, Form
from fastapi.responses import JSONResponse
from fastapi.datastructures import Default
from PIL import Image
import io
import base64
import httpx
from typing import Optional, Union, Annotated
from ..application.dtos.vision_dtos import (
    TaskType,
    ImageRequestDTO,
    DetectionResponseDTO as DetectionResponse,
    CaptionResponseDTO as CaptionResponse,
    OCRResponseDTO as OCRResponse,
    AnalyzeImageResponseDTO,
    AnalyzeImageRequestDTO,
)
from ..application.use_cases.analyze_image import AnalyzeImageInput, AnalyzeImageUseCase
from ..core.di.container import (
    get_yolo,
    get_blip,
    get_easyocr,
    get_analyze_image_use_case,
)
from ..core.config.settings import get_settings
from ..domain.ports import IObjectDetector, IImageCaptioner, IOCRProcessor

router = APIRouter(prefix="/vision", tags=["vision"])


async def load_image_from_bytes(contents: bytes) -> Image.Image:
    settings = get_settings()
    if len(contents) > settings.MAX_IMAGE_SIZE:
        raise HTTPException(400, f"Image too large (max {settings.MAX_IMAGE_SIZE // 1024 // 1024}MB)")
    try:
        return Image.open(io.BytesIO(contents)).convert("RGB")
    except Exception:
        raise HTTPException(400, "Invalid image file")


async def load_image(file: UploadFile) -> Image.Image:
    contents = await file.read()
    return await load_image_from_bytes(contents)


async def load_image_from_url(url: str) -> Image.Image:
    """Fetch image from URL and load it."""
    async with httpx.AsyncClient(timeout=30.0) as client:
        response = await client.get(url, headers={"User-Agent": "Mozilla/5.0"})
        response.raise_for_status()
        contents = response.content
    return await load_image_from_bytes(contents)


async def load_image_from_base64(base64_string: str) -> Image.Image:
    """Decode base64 string and load image."""
    cleaned_base64 = base64_string
    if "," in base64_string:
        cleaned_base64 = base64_string.split(",", 1)[1]
    try:
        image_data = base64.b64decode(cleaned_base64)
    except Exception:
        raise HTTPException(400, "Invalid base64 image data")
    return await load_image_from_bytes(image_data)


async def load_image_from_request(request: ImageRequestDTO) -> Image.Image:
    """Load image from request (URL or base64)."""
    if request.has_image_url():
        return await load_image_from_url(request.imageUrl)
    elif request.has_image():
        return await load_image_from_base64(request.image)
    else:
        raise HTTPException(400, "No image data provided. Use 'imageUrl' or 'image' field.")


@router.post("/detect", response_model=DetectionResponse)
async def detect_objects(
    request: Request,
    conf: float = 0.25,
    detector: IObjectDetector = Depends(get_yolo)
):
    content_type = request.headers.get("content-type", "")
    image = None
    
    if "application/json" in content_type:
        body = await request.body()
        import json
        data = json.loads(body)
        image_request = ImageRequestDTO(**data) if data else None
        if image_request and image_request.has_image_data():
            image = await load_image_from_request(image_request)
            conf = image_request.confidence or conf
    elif "multipart/form-data" in content_type:
        form = await request.form()
        file = form.get("image")
        if file:
            image = await load_image(file)
    
    if image is None:
        raise HTTPException(400, "Either JSON body with imageUrl/image or multipart file is required")
    return await detector.detect(image, conf_threshold=conf)


@router.post("/caption", response_model=CaptionResponse)
async def caption_image(
    request: Request,
    captioner: IImageCaptioner = Depends(get_blip)
):
    content_type = request.headers.get("content-type", "")
    image = None
    
    if "application/json" in content_type:
        body = await request.body()
        import json
        data = json.loads(body)
        image_request = ImageRequestDTO(**data) if data else None
        if image_request and image_request.has_image_data():
            image = await load_image_from_request(image_request)
    elif "multipart/form-data" in content_type:
        form = await request.form()
        file = form.get("image")
        if file:
            image = await load_image(file)
    
    if image is None:
        raise HTTPException(400, "Either JSON body with imageUrl/image or multipart file is required")
    return await captioner.caption(image)


@router.post("/ocr", response_model=OCRResponse)
async def extract_text(
    request: Request,
    ocr: IOCRProcessor = Depends(get_easyocr),
    engine: str = "easyocr"
):
    content_type = request.headers.get("content-type", "")
    image = None
    
    if "application/json" in content_type:
        body = await request.body()
        import json
        data = json.loads(body)
        image_request = ImageRequestDTO(**data) if data else None
        if image_request and image_request.has_image_data():
            image = await load_image_from_request(image_request)
            engine = image_request.engine or engine
    elif "multipart/form-data" in content_type:
        form = await request.form()
        file = form.get("image")
        if file:
            image = await load_image(file)
    
    if image is None:
        raise HTTPException(400, "Either JSON body with imageUrl/image or multipart file is required")
    return await ocr.extract_text(image, engine=engine)


@router.post("/analyze", response_model=AnalyzeImageResponseDTO)
async def analyze_image(
    request: Request,
    task: TaskType = TaskType.CAPTION_IMAGE,
    use_case: AnalyzeImageUseCase = Depends(get_analyze_image_use_case),
):
    content_type = request.headers.get("content-type", "")
    image = None
    
    if "application/json" in content_type:
        body = await request.body()
        import json
        data = json.loads(body)
        image_request = ImageRequestDTO(**data) if data else None
        if image_request and image_request.has_image_data():
            image = await load_image_from_request(image_request)
            task = image_request.task or task
    elif "multipart/form-data" in content_type:
        form = await request.form()
        file = form.get("image")
        if file:
            image = await load_image(file)
    
    if image is None:
        raise HTTPException(400, "Either JSON body with imageUrl/image or multipart file is required")
    input_data = AnalyzeImageInput(image=image, task=task)
    return await use_case.execute(input_data)
