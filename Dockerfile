FROM golang:1.13.4 as build

COPY . .

RUN go build -o main main.go

FROM scratch

COPY --from=build main main
CMD ["./main"]