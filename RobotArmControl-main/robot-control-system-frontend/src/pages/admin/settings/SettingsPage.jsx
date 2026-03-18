import React from "react";
import { Card, Typography } from "antd";

const { Title, Paragraph } = Typography;

export default function SettingsPage() {
  return (
    <div>
      <div style={{ marginBottom: 16 }}>
        <Title level={3} style={{ margin: 0 }}>
          Cài đặt 
        </Title>
        <Paragraph style={{ margin: 0 }}>
          Chưa có chức năng
        </Paragraph>
      </div>

      <Card>Settings Content Here</Card>
    </div>
  );
}