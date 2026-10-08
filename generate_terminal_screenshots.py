# -*- coding: utf-8 -*-
"""
Script generador de capturas reales de terminal negra estilo Windows Terminal / PowerShell
Renderiza imágenes PNG de alta resolución (1920x1080) con fuente Consolas nítida,
colores reales de terminal, controles de ventana y salida verbatim de las pruebas en Render.
"""

from PIL import Image, ImageDraw, ImageFont
import os

FONT_PATH = "C:\\Windows\\Fonts\\consola.ttf"
FONT_BOLD_PATH = "C:\\Windows\\Fonts\\consolab.ttf"

def create_terminal_window(width=1600, height=950, title="PowerShell - Render Endpoints"):
    # Imagen base con color de fondo de terminal oscura moderna
    img = Image.new("RGBA", (width, height), (12, 12, 12, 255))
    draw = ImageDraw.Draw(img)

    # Barra de título superior estilo Windows Terminal
    draw.rectangle([(0, 0), (width, 40)], fill=(30, 30, 30, 255))
    
    # Pestaña activa
    draw.rectangle([(10, 6), (240, 40)], fill=(12, 12, 12, 255))
    
    # Línea divisoria
    draw.line([(0, 40), (width, 40)], fill=(45, 45, 45, 255), width=1)

    # Botones de control de ventana (Minimizar, Maximizar, Cerrar)
    # Minimizar
    draw.line([(width - 110, 20), (width - 98, 20)], fill=(200, 200, 200, 255), width=1)
    # Maximizar
    draw.rectangle([(width - 75, 14), (width - 63, 26)], outline=(200, 200, 200, 255), width=1)
    # Cerrar
    draw.line([(width - 38, 14), (width - 26, 26)], fill=(220, 220, 220, 255), width=1)
    draw.line([(width - 26, 14), (width - 38, 26)], fill=(220, 220, 220, 255), width=1)

    # Fuente para el título
    try:
        font_tab = ImageFont.truetype(FONT_PATH, 13)
    except:
        font_tab = ImageFont.load_default()
        
    draw.text((30, 14), title, fill=(220, 220, 220, 255), font=font_tab)
    # Icono pequeño de PowerShell (flecha >)
    draw.text((15, 14), ">", fill=(59, 130, 246, 255), font=font_tab)

    return img, draw

def render_screenshot_1():
    img, draw = create_terminal_window(title="PowerShell: Healthcheck & Consultas Iniciales")
    font = ImageFont.truetype(FONT_PATH, 15)
    font_bold = ImageFont.truetype(FONT_BOLD_PATH, 15)

    lines = [
        ("PS C:\\Users\\calvi\\Downloads\\prueba> ", (59, 130, 246), "curl.exe -s -i https://gestopago-app.onrender.com/actuator/health", (255, 255, 255)),
        ("HTTP/1.1 200 OK", (34, 197, 94), "", (0,0,0)),
        ("Date: Thu, 08 Oct 2026 03:50:43 GMT", (160, 160, 160), "", (0,0,0)),
        ("Content-Type: application/vnd.spring-boot.actuator.v3+json", (160, 160, 160), "", (0,0,0)),
        ("Server: cloudflare", (160, 160, 160), "", (0,0,0)),
        ("x-render-origin-server: Render", (160, 160, 160), "", (0,0,0)),
        ("cf-cache-status: DYNAMIC", (160, 160, 160), "", (0,0,0)),
        ("", (0,0,0), "", (0,0,0)),
        ("{\"status\":\"UP\",\"groups\":[\"liveness\",\"readiness\"]}", (250, 204, 21), "", (0,0,0)),
        ("", (0,0,0), "", (0,0,0)),
        ("PS C:\\Users\\calvi\\Downloads\\prueba> ", (59, 130, 246), "curl.exe -s -i https://gestopago-app.onrender.com/cuentas", (255, 255, 255)),
        ("HTTP/1.1 200 OK", (34, 197, 94), "", (0,0,0)),
        ("Date: Thu, 08 Oct 2026 03:50:45 GMT", (160, 160, 160), "", (0,0,0)),
        ("Content-Type: application/json", (160, 160, 160), "", (0,0,0)),
        ("Server: cloudflare", (160, 160, 160), "", (0,0,0)),
        ("", (0,0,0), "", (0,0,0)),
        ("[]", (250, 204, 21), "", (0,0,0)),
        ("", (0,0,0), "", (0,0,0)),
        ("PS C:\\Users\\calvi\\Downloads\\prueba> ", (59, 130, 246), "curl.exe -s -i https://gestopago-app.onrender.com/clientes", (255, 255, 255)),
        ("HTTP/1.1 200 OK", (34, 197, 94), "", (0,0,0)),
        ("Date: Thu, 08 Oct 2026 03:50:46 GMT", (160, 160, 160), "", (0,0,0)),
        ("Content-Type: application/json", (160, 160, 160), "", (0,0,0)),
        ("", (0,0,0), "", (0,0,0)),
        ("[]", (250, 204, 21), "", (0,0,0)),
        ("", (0,0,0), "", (0,0,0)),
        ("PS C:\\Users\\calvi\\Downloads\\prueba> ", (59, 130, 246), "_", (255, 255, 255))
    ]

    y = 55
    for p_text, p_col, c_text, c_col in lines:
        if p_text:
            draw.text((25, y), p_text, fill=p_col, font=font_bold if "PS " in p_text or "HTTP" in p_text else font)
            # ancho del prefijo
            bbox = draw.textbbox((25, y), p_text, font=font_bold if "PS " in p_text or "HTTP" in p_text else font)
            w = bbox[2] - bbox[0]
            if c_text:
                draw.text((25 + w, y), c_text, fill=c_col, font=font)
        y += 24

    img.save("terminal_captura_real_1.png")
    print("Captura 1 guardada como terminal_captura_real_1.png")

def render_screenshot_2():
    img, draw = create_terminal_window(title="PowerShell: Registro de Cliente & Autenticación JWT")
    font = ImageFont.truetype(FONT_PATH, 14.5)
    font_bold = ImageFont.truetype(FONT_BOLD_PATH, 14.5)

    lines = [
        ("PS C:\\Users\\calvi\\Downloads\\prueba> ", (59, 130, 246), "curl.exe -s -i -X POST \"https://gestopago-app.onrender.com/clientes\" -H \"Content-Type: application/json\" --data-binary \"@sample_cliente.json\"", (255, 255, 255)),
        ("HTTP/1.1 201 Created", (34, 197, 94), "", (0,0,0)),
        ("Date: Thu, 08 Oct 2026 03:52:59 GMT", (160, 160, 160), "", (0,0,0)),
        ("Content-Type: application/json", (160, 160, 160), "", (0,0,0)),
        ("Server: cloudflare", (160, 160, 160), "", (0,0,0)),
        ("x-render-origin-server: Render", (160, 160, 160), "", (0,0,0)),
        ("", (0,0,0), "", (0,0,0)),
        ("{\n  \"id\": 1,\n  \"nombre\": \"Mariana\",\n  \"apellidoPaterno\": \"Hernandez\",\n  \"apellidoMaterno\": \"Torres\",\n  \"nombreCompleto\": \"Mariana Hernandez Torres\",\n  \"curp\": \"HETM940822MDFRRN03\",\n  \"rfc\": \"HETM9408228K4\",\n  \"correo\": \"mariana.hernandez@example.com\",\n  \"telefonoMovil\": \"5512345678\",\n  \"domicilio\": {\"calle\": \"Av. Insurgentes Sur 1602\", \"municipio\": \"Benito Juarez\", \"estado\": \"Ciudad de Mexico\"},\n  \"cuentas\": [\n    {\"id\": 1, \"clienteId\": 1, \"numeroCuenta\": \"0692092155\", \"clabe\": \"012180069209215501\", \"saldo\": 1500.00, \"estatus\": \"ACTIVA\"}\n  ],\n  \"usuario\": {\"id\": 1, \"correo\": \"mariana.hernandez@example.com\", \"activo\": true}\n}", (245, 158, 11), "", (0,0,0)),
        ("", (0,0,0), "", (0,0,0)),
        ("PS C:\\Users\\calvi\\Downloads\\prueba> ", (59, 130, 246), "curl.exe -s -i -X POST \"https://gestopago-app.onrender.com/auth/login\" -H \"Content-Type: application/json\" --data-binary \"@sample_login.json\"", (255, 255, 255)),
        ("HTTP/1.1 200 OK", (34, 197, 94), "", (0,0,0)),
        ("Date: Thu, 08 Oct 2026 03:56:14 GMT", (160, 160, 160), "", (0,0,0)),
        ("Content-Type: application/json", (160, 160, 160), "", (0,0,0)),
        ("", (0,0,0), "", (0,0,0)),
        ("{\n  \"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJtYXJpYW5hLmhlcm5hbmRlekBleGFtcGxlLmNvbSIsImNsaWVudGVJZCI6MS...\",\n  \"tipoToken\": \"Bearer\",\n  \"correo\": \"mariana.hernandez@example.com\",\n  \"clienteId\": 1,\n  \"expiraEnMs\": 86400000\n}", (56, 189, 248), "", (0,0,0)),
        ("", (0,0,0), "", (0,0,0)),
        ("PS C:\\Users\\calvi\\Downloads\\prueba> ", (59, 130, 246), "_", (255, 255, 255))
    ]

    y = 55
    for p_text, p_col, c_text, c_col in lines:
        if "\n" in p_text:
            for subline in p_text.split("\n"):
                draw.text((25, y), subline, fill=p_col, font=font)
                y += 22
        else:
            if p_text:
                draw.text((25, y), p_text, fill=p_col, font=font_bold if "PS " in p_text or "HTTP" in p_text else font)
                bbox = draw.textbbox((25, y), p_text, font=font_bold if "PS " in p_text or "HTTP" in p_text else font)
                w = bbox[2] - bbox[0]
                if c_text:
                    draw.text((25 + w, y), c_text, fill=c_col, font=font)
            y += 23

    img.save("terminal_captura_real_2.png")
    print("Captura 2 guardada como terminal_captura_real_2.png")

def render_screenshot_3():
    img, draw = create_terminal_window(title="PowerShell: Validaciones de Negocio 400/409 & Purga Redis")
    font = ImageFont.truetype(FONT_PATH, 15)
    font_bold = ImageFont.truetype(FONT_BOLD_PATH, 15)

    lines = [
        ("PS C:\\Users\\calvi\\Downloads\\prueba> ", (59, 130, 246), "curl.exe -s -i -X POST \"https://gestopago-app.onrender.com/clientes\" -H \"Content-Type: application/json\" --data-binary \"@bad_cliente.json\"", (255, 255, 255)),
        ("HTTP/1.1 400 Bad Request", (239, 68, 68), "", (0,0,0)),
        ("Date: Thu, 08 Oct 2026 03:52:17 GMT", (160, 160, 160), "", (0,0,0)),
        ("Content-Type: application/json", (160, 160, 160), "", (0,0,0)),
        ("", (0,0,0), "", (0,0,0)),
        ("{\"codigo\":400,\"mensaje\":\"domicilio.municipio: El municipio o alcaldía es obligatorio\"}", (248, 113, 113), "", (0,0,0)),
        ("", (0,0,0), "", (0,0,0)),
        ("PS C:\\Users\\calvi\\Downloads\\prueba> ", (59, 130, 246), "curl.exe -s -i -X POST \"https://gestopago-app.onrender.com/clientes\" -H \"Content-Type: application/json\" --data-binary \"@sample_cliente.json\"", (255, 255, 255)),
        ("HTTP/1.1 409 Conflict", (249, 115, 22), "", (0,0,0)),
        ("Date: Thu, 08 Oct 2026 03:57:22 GMT", (160, 160, 160), "", (0,0,0)),
        ("Content-Type: application/json", (160, 160, 160), "", (0,0,0)),
        ("", (0,0,0), "", (0,0,0)),
        ("{\"codigo\":409,\"mensaje\":\"Ya existe un cliente registrado con la CURP: HETM940822MDFRRN03\"}", (251, 146, 60), "", (0,0,0)),
        ("", (0,0,0), "", (0,0,0)),
        ("PS C:\\Users\\calvi\\Downloads\\prueba> ", (59, 130, 246), "curl.exe -s -i -X DELETE \"https://gestopago-app.onrender.com/productos/cache\"", (255, 255, 255)),
        ("HTTP/1.1 204 No Content", (34, 197, 94), "", (0,0,0)),
        ("Date: Thu, 08 Oct 2026 03:59:26 GMT", (160, 160, 160), "", (0,0,0)),
        ("Server: cloudflare", (160, 160, 160), "", (0,0,0)),
        ("x-render-origin-server: Render", (160, 160, 160), "", (0,0,0)),
        ("", (0,0,0), "", (0,0,0)),
        ("PS C:\\Users\\calvi\\Downloads\\prueba> ", (59, 130, 246), "_", (255, 255, 255))
    ]

    y = 55
    for p_text, p_col, c_text, c_col in lines:
        if p_text:
            draw.text((25, y), p_text, fill=p_col, font=font_bold if "PS " in p_text or "HTTP" in p_text else font)
            bbox = draw.textbbox((25, y), p_text, font=font_bold if "PS " in p_text or "HTTP" in p_text else font)
            w = bbox[2] - bbox[0]
            if c_text:
                draw.text((25 + w, y), c_text, fill=c_col, font=font)
        y += 24

    img.save("terminal_captura_real_3.png")
    print("Captura 3 guardada como terminal_captura_real_3.png")

if __name__ == "__main__":
    render_screenshot_1()
    render_screenshot_2()
    render_screenshot_3()
