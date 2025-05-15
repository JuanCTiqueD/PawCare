# 📦 PawCare

**Universidad:** Jorge Tadeo Lozano  
**Tecnología Base de Datos:** Firebase (Cloud Firestore)  
**Plataforma:** Kotlin para Android  
**Nombre del Proyecto:** PawCare  
**Tipo de Aplicación:** Gestión de mascotas y servicios de cuidado

---

## 📚 Tabla de Contenidos

1. [Descripción del Proyecto](#descripción-del-proyecto)
2. [Estructura de la Base de Datos](#estructura-de-la-base-de-datos)
   - [1. Users (Usuarios)](#1-users-usuarios)
   - [2. Pets (Mascotas)](#2-pets-mascotas)
   - [3. Vaccines (Vacunas)](#3-vaccines-vacunas)
   - [4. Caregivers (Cuidadores)](#4-caregivers-cuidadores)
   - [5. Reviews (Reseñas)](#5-reviews-reseñas)
   - [6. Bookings (Reservas)](#6-bookings-reservas)
3. [Integrantes del Equipo](#integrantes-del-equipo)

---

## 📖 Descripción del Proyecto

PawCare es una aplicación móvil desarrollada en Kotlin para Android, que permite a los usuarios gestionar la información de sus mascotas, agendar servicios de cuidado con cuidadores certificados, llevar el control de vacunas, y evaluar los servicios recibidos.  
La base de datos utilizada es **Firebase Cloud Firestore**, estructurada de forma no relacional para facilitar la escalabilidad y la sincronización en tiempo real.

---

## 🗃️ Estructura de la Base de Datos

### 1. `users` (Usuarios)
| Campo | Tipo | Descripción |
|-------|------|-------------|
| `userId` | string | ID de Firebase Auth. |
| `email` | string | Correo electrónico. |
| `role` | string | Rol del usuario: `"user"` o `"caregiver"`. |
| `username` | string | Nombre público del usuario. |
| `profileImage` | string | URL de la imagen de perfil. |
| `location` | string | Ubicación del usuario. |
| `createdAt` | timestamp | Fecha de creación. |
| `lastModifiedAt` | timestamp | Última modificación. |

### 2. `pets` (Mascotas)
| Campo | Tipo | Descripción |
|-------|------|-------------|
| `petId` | string | ID único de la mascota. |
| `userId` | string | ID del dueño (users). |
| `name` | string | Nombre de la mascota. |
| `species` | string | Especie (perro, gato, etc). |
| `breed` | string | Raza. |
| `sex` | string | Sexo: macho o hembra. |
| `birthDate` | string | Fecha de nacimiento (YYYY-MM-DD). |
| `weight` | float | Peso en kilogramos. |
| `diseases` | array<string> | Enfermedades conocidas. |
| `allergies` | array<string> | Alergias. |
| `profileImage` | string | URL imagen de la mascota. |
| `createdAt` | timestamp | Fecha de creación. |
| `lastModifiedAt` | timestamp | Última modificación. |

### 3. `vaccines` (Vacunas)
📍 Subcolección: `pets/{petId}/vaccines/{vaccineId}`

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `vaccineId` | string | ID de la vacuna. |
| `name` | string | Nombre de la vacuna. |
| `dateApplied` | string | Fecha aplicada. |
| `nextDose` | string | Fecha próxima dosis. |
| `veterinary` | string | Clínica veterinaria. |
| `notes` | string | Notas adicionales. |
| `createdAt` | timestamp | Fecha de creación. |

### 4. `caregivers` (Cuidadores)
| Campo | Tipo | Descripción |
|-------|------|-------------|
| `userId` | string | ID del cuidador (igual a users). |
| `description` | string | Descripción personal. |
| `experience` | string | Años de experiencia. |
| `services` | array<string> | Servicios ofrecidos. |
| `hourlyRate` | float | Precio por hora. |
| `certifications` | array<string> | Certificaciones. |
| `acceptedPetSizes` | object | Peso mínimo y máximo aceptado. |
| `specialties` | array<string> | Especialidades (opcional). |
| `availability` | map | Horarios disponibles. |
| `gallery` | array<string> | Fotos instalaciones o servicios. |
| `rating` | float | Promedio de reseñas. |
| `reviewsCount` | int | Número de reseñas. |
| `completedServices` | int | Servicios realizados. |
| `location` | string | Ciudad base. |
| `geoPoint` | GeoPoint | Coordenadas de búsqueda. |
| `createdAt` | timestamp | Fecha de creación. |
| `lastModifiedAt` | timestamp | Última modificación. |

### 5. `reviews` (Reseñas)
| Campo | Tipo | Descripción |
|-------|------|-------------|
| `reviewId` | string | ID de la reseña. |
| `userId` | string | Usuario que comentó. |
| `petId` | string | Mascota involucrada (opcional). |
| `rating` | int | Calificación (1 a 5). |
| `comment` | string | Comentario del usuario. |
| `serviceType` | string | Servicio evaluado. |
| `createdAt` | timestamp | Fecha de creación. |

### 6. `bookings` (Reservas)
| Campo | Tipo | Descripción |
|-------|------|-------------|
| `bookingId` | string | ID de la reserva. |
| `userId` | string | Usuario que reserva. |
| `petId` | string | Mascota involucrada. |
| `service` | string | Servicio solicitado. |
| `date` | string | Fecha de la reserva. |
| `timeSlot` | string | Franja horaria. |
| `status` | string | Estado: confirmed, cancelled, completed. |
| `createdAt` | timestamp | Fecha de creación. |

---

## 👥 Integrantes del Equipo

- Valentina Caicedo - Desarrolladora Frontend
- Leonardo Leon - Desarrollador Frontend
- Laura Sofia Castañeda - Desarrolladora Frontend
- Juan Camilo Tique - Desarrollador Backend

---


### Integrantes
Valentina Caicedo
Leonardo Leon
